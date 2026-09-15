package com.sapo;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

public class SapoPortals {
   private static final java.util.Set<Integer> seen = new java.util.HashSet();
   private static int cooldown = 0;

   public static void registrar() {
      ClientTickEvents.END_CLIENT_TICK.register(SapoPortals::onTick);
   }

   private static void onTick(Minecraft client) {
      if (!Config.splits || client.level == null || client.player == null) {
         seen.clear();
      } else if (--cooldown <= 0) {
         cooldown = 5;
         double range = (double)Math.max(8, Config.portalRange);

         for(Entity entity : client.level.getEntities(client.player, client.player.getBoundingBox().inflate(range))) {
            if (!((double)entity.distanceTo(client.player) > range)) {
               String gear = SapoGear.model(entity);
               if (!gear.isEmpty() && SapoDisplays.matchesAny(gear, Config.parsedPortalModels) && seen.add(entity.getId())) {
                  SapoSplits.onPortal(gear, secondsAbove(client, entity));
               }
            }
         }

      }
   }

   private static int secondsAbove(Minecraft client, Entity portal) {
      AABB box = new AABB(portal.getX() - 1.5, portal.getY(), portal.getZ() - 1.5, portal.getX() + 1.5, portal.getY() + 7.0, portal.getZ() + 1.5);

      for(Entity entity : client.level.getEntities(portal, box)) {
         if (entity.hasCustomName()) {
            int seconds = parseTimer(SapoAutoClicker.sanitize(entity.getCustomName().getString()));
            if (seconds >= 0) {
               return seconds;
            }
         }
      }

      return -1;
   }

   private static int parseTimer(String name) {
      String text = name == null ? "" : name.trim();
      if (text.length() >= 5 && text.startsWith("-=") && text.endsWith("=-")) {
         String body = text.substring(2, text.length() - 2).trim();
         if (body.length() >= 3 && body.charAt(0) == '[' && body.charAt(body.length() - 1) == ']') {
            body = body.substring(1, body.length() - 1).trim();
         }

         if (!body.isEmpty() && body.length() <= 3) {
            for(int i = 0; i < body.length(); ++i) {
               if (body.charAt(i) < '0' || body.charAt(i) > '9') {
                  return -1;
               }
            }

            return Integer.parseInt(body);
         } else {
            return -1;
         }
      } else {
         return -1;
      }
   }
}

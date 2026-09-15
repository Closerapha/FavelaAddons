package com.sapo;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class SapoTrapCounter {
   private static final int GREEN = -11141291;
   private static final int YELLOW = -171;
   private static final int RED = -43691;
   private static final int WARNING_DURATION = 40;
   private static final String WARNING_TEXT = "Max traps!";
   private static final String TRAP_ITEM = "trap";
   private static int trapCount = 0;
   private static int warningTicks = 0;

   public static void registrar() {
      ClientTickEvents.END_CLIENT_TICK.register(SapoTrapCounter::onTick);
      HudElementRegistry.addLast(Identifier.parse("sapo:trap_counter"), SapoTrapCounter::render);
   }

   private static void onTick(Minecraft client) {
      if (warningTicks > 0) {
         --warningTicks;
      }

      if (Config.trapCounter && client.level != null && client.player != null) {
         if (client.player.tickCount % 5 == 0) {
            try {
               trapCount = countTraps(client);
            } catch (Exception e) {
               if (Config.devMode) {
                  System.out.println("[FA Traps] Error counting: " + e.getMessage());
               }
            }
         }

      } else {
         trapCount = 0;
      }
   }

   private static int countTraps(Minecraft client) {
      double range = (double)Math.max(1, Config.trapCounterRange);
      AABB box = client.player.getBoundingBox().inflate(range);
      Set<String> instances = new HashSet();

      for(Entity entity : client.level.getEntities(client.player, box)) {
         String modelId = SapoDisplays.modelId(entity);
         if (SapoDisplays.matchesAny(modelId, Config.parsedTrapCounterModels)) {
            Entity root = entity;

            while(root.getVehicle() != null) {
               root = root.getVehicle();
            }

            if (root != entity) {
               instances.add("v" + root.getId());
            } else {
               Vec3 pos = SapoDisplays.renderedPosition(entity);
               instances.add(String.format(Locale.ROOT, "p%.1f/%.1f/%.1f", pos.x, pos.y, pos.z));
            }
         }
      }

      return instances.size();
   }


   public static boolean shouldBlockUse(Player player, InteractionHand hand) {
      if (Config.trapCounter && Config.trapBlockAtMax && player != null && !player.isShiftKeyDown()) {
         if (trapCount < Math.max(1, Config.trapCounterMax)) {
            return false;
         } else {
            ItemStack held = hand == InteractionHand.OFF_HAND ? player.getOffhandItem() : player.getMainHandItem();
            String modelId = SapoDisplays.modelId(held);
            if (modelId != null && modelId.toLowerCase().contains(TRAP_ITEM)) {
               warningTicks = WARNING_DURATION;
               return true;
            } else {
               return false;
            }
         }
      } else {
         return false;
      }
   }

   private static void render(GuiGraphicsExtractor graphics, DeltaTracker tracker) {
      Minecraft client = Minecraft.getInstance();
      if (Config.trapCounter && client.player != null && (trapCount > 0 || warningTicks > 0 || !Config.trapCounterHideEmpty)) {
         int max = Math.max(1, Config.trapCounterMax);
         String text = Config.trapCounterLabel + " " + trapCount;
         int color = trapCount >= max ? RED : (trapCount >= max * 3 / 4 ? YELLOW : GREEN);
         Font font = client.font;
         graphics.pose().pushMatrix();
         graphics.pose().translate((float)Config.trapCounterX, (float)Config.trapCounterY);
         graphics.pose().scale(Config.trapCounterScale, Config.trapCounterScale);
         graphics.text(font, text, 0, 0, color, true);
         if (warningTicks > 0) {
            graphics.text(font, WARNING_TEXT, 0, 10, RED, true);
         }

         graphics.pose().popMatrix();
      }
   }
}

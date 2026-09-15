package com.favelaaddons;

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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class FavelaTrapCounter {
   private static final int GREEN = -11141291;
   private static final int YELLOW = -171;
   private static final int RED = -43691;
   private static final String TRAP_ITEM = "trap";
   private static int trapCount = 0;

   public static void registrar() {
      ClientTickEvents.END_CLIENT_TICK.register(FavelaTrapCounter::onTick);
      HudElementRegistry.addLast(Identifier.parse("favelaaddons:trap_counter"), FavelaTrapCounter::render);
   }

   private static void onTick(Minecraft client) {
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
         String modelId = FavelaDisplays.modelId(entity);
         if (FavelaDisplays.matchesAny(modelId, Config.parsedTrapCounterModels)) {
            Entity root = entity;

            while(root.getVehicle() != null) {
               root = root.getVehicle();
            }

            if (root != entity) {
               instances.add("v" + root.getId());
            } else {
               Vec3 pos = FavelaDisplays.renderedPosition(entity);
               instances.add(String.format(Locale.ROOT, "p%.1f/%.1f/%.1f", pos.x, pos.y, pos.z));
            }
         }
      }

      return instances.size();
   }


   private static void render(GuiGraphicsExtractor graphics, DeltaTracker tracker) {
      Minecraft client = Minecraft.getInstance();
      if (Config.trapCounter && client.player != null && (trapCount > 0 || !Config.trapCounterHideEmpty)) {
         int max = Math.max(1, Config.trapCounterMax);
         String text = Config.trapCounterLabel + " " + trapCount;
         int color = trapCount >= max ? RED : (trapCount >= max * 3 / 4 ? YELLOW : GREEN);
         Font font = client.font;
         graphics.pose().pushMatrix();
         graphics.pose().translate((float)Config.trapCounterX, (float)Config.trapCounterY);
         graphics.pose().scale(Config.trapCounterScale, Config.trapCounterScale);
         graphics.text(font, text, 0, 0, color, true);
         graphics.pose().popMatrix();
      }
   }
}

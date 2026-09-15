package com.favelaaddons;

import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Interaction;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;

public class FavelaESP {
   private static final int RED = -65536;
   private static final int ORANGE = -28672;
   private static final int CYAN = -16711681;
   private static final int MAGENTA = -65281;
   private static final int YELLOW = -256;
   private static final float STROKE_WIDTH = 3.0F;

   public static void registrar() {
      LevelRenderEvents.BEFORE_GIZMOS.register(FavelaESP::render);
   }

   private static void render(LevelRenderContext context) {
      if (Config.boneEsp || Config.carrotStickEsp || Config.stickEsp || Config.armorStandEsp || Config.interactionEsp) {
         Minecraft client = Minecraft.getInstance();
         if (client.level != null && client.player != null) {
            double range = (double)Math.max(1, Config.espRange);
            AABB search = client.player.getBoundingBox().inflate(range);

            try {
               for(Entity entity : client.level.getEntities(client.player, search)) {
                  int color = markerColor(entity);
                  if (color != 0 && (double)entity.distanceTo(client.player) <= range) {
                     Gizmos.cuboid(markerBox(entity), GizmoStyle.strokeAndFill(color, STROKE_WIDTH, color)).setAlwaysOnTop();
                  }
               }
            } catch (Exception e) {
               if (Config.devMode) {
                  System.out.println("[FA ESP] Error rendering markers: " + e.getMessage());
               }
            }

         }
      }
   }

   private static int markerColor(Entity entity) {
      if (entity instanceof ArmorStand) {
         return Config.armorStandEsp ? MAGENTA : 0;
      } else if (entity instanceof Interaction) {
         return Config.interactionEsp ? YELLOW : 0;
      } else if (!(entity instanceof Display.ItemDisplay)) {
         return 0;
      } else {
         ItemStack stack = ((Display.ItemDisplay)entity).getItemStack();
         if (stack != null && !stack.isEmpty()) {
            Item item = stack.getItem();
            if (item == Items.BONE) {
               return Config.boneEsp ? RED : 0;
            } else if (item == Items.CARROT_ON_A_STICK) {
               return Config.carrotStickEsp ? ORANGE : 0;
            } else {
               return item == Items.STICK && Config.stickEsp ? CYAN : 0;
            }
         } else {
            return 0;
         }
      }
   }

   private static AABB markerBox(Entity entity) {
      AABB box = entity.getBoundingBox();
      if (box.getXsize() >= 0.2 && box.getYsize() >= 0.2 && box.getZsize() >= 0.2) {
         return box;
      } else {
         double x = entity.getX();
         double y = entity.getY();
         double z = entity.getZ();
         return new AABB(x - 0.3, y - 0.3, z - 0.3, x + 0.3, y + 0.3, z + 0.3);
      }
   }
}

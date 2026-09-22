package com.favelaaddons;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3fc;

public class FavelaMonolith {
   private static final String ANCHOR_BONE = "/body";
   private static final String TWIN_BONE = "/body2";
   private static final double SCAN_RANGE = 48.0;
   private static final double TWIN_GAP = 0.6;
   private static final double VISIBLE_SCALE = 0.02;
   private static final int RING_STEPS = 48;
   private static final float RING_WIDTH = 2.0F;
   private static final int ACTIVE_TINT = -16711792;
   private static final int LABEL = -1;
   private static final int ACTIVE_LABEL = -16711792;
   private static final List<Pillar> pillars = new ArrayList();

   public static void registrar() {
      ClientTickEvents.END_CLIENT_TICK.register(FavelaMonolith::onTick);
      LevelRenderEvents.BEFORE_GIZMOS.register(FavelaMonolith::render);
      HudElementRegistry.addLast(Identifier.parse("favelaaddons:monolith"), FavelaMonolith::hud);
   }

   private static class Pillar {
      Vec3 at;
      boolean active;
   }

   private static boolean bone(Entity entity) {
      if (!(entity instanceof Display.ItemDisplay)) {
         return false;
      } else {
         String model = FavelaDisplays.modelId(entity);
         return model != null && FavelaDisplays.matchesAny(model, Config.parsedMonolithModels);
      }
   }

   private static boolean named(Entity entity, String tail) {
      String model = FavelaDisplays.modelId(entity);
      return model != null && model.toLowerCase(Locale.ROOT).endsWith(tail);
   }

   private static double biggestScale(Entity entity) {
      try {
         Vector3fc scale = (Vector3fc)entity.getEntityData().get(Display.DATA_SCALE_ID);
         if (scale == null) {
            return 0.0;
         } else {
            return Math.max(Math.abs((double)scale.x()), Math.max(Math.abs((double)scale.y()), Math.abs((double)scale.z())));
         }
      } catch (Exception e) {
         return 0.0;
      }
   }

   private static void onTick(Minecraft client) {
      if (FavelaPower.off()) {
         return;
      }

      if (!Config.monolith || client.level == null || client.player == null) {
         if (!pillars.isEmpty()) {
            pillars.clear();
         }

         return;
      }

      List<Entity> anchors = new ArrayList();
      List<Entity> twins = new ArrayList();
      AABB search = client.player.getBoundingBox().inflate(SCAN_RANGE);

      try {
         for(Entity entity : client.level.getEntities(client.player, search)) {
            if (bone(entity)) {
               if (named(entity, ANCHOR_BONE)) {
                  anchors.add(entity);
               } else if (named(entity, TWIN_BONE)) {
                  twins.add(entity);
               }
            }
         }
      } catch (Exception e) {
      }

      List<Pillar> found = new ArrayList();

      for(Entity anchor : anchors) {
         Vec3 at = FavelaDisplays.renderedPosition(anchor);
         double mine = biggestScale(anchor);
         double theirs = 0.0;

         for(Entity twin : twins) {
            if (FavelaDisplays.renderedPosition(twin).distanceTo(at) <= TWIN_GAP) {
               theirs = Math.max(theirs, biggestScale(twin));
            }
         }

         Pillar pillar = new Pillar();
         pillar.at = at;
         pillar.active = mine > VISIBLE_SCALE && mine >= theirs;
         found.add(pillar);
      }

      pillars.clear();
      pillars.addAll(found);
   }

   private static void render(LevelRenderContext context) {
      if (FavelaPower.off()) {
         return;
      }

      if (Config.monolith && Config.monolithRing && !pillars.isEmpty()) {
         try {
            double radius = (double)Math.max(1, Config.monolithRadius);

            for(Pillar pillar : pillars) {
               int colour = (pillar.active ? ACTIVE_TINT : Config.monolithColor) | -16777216;
               Vec3 previous = null;

               for(int step = 0; step <= RING_STEPS; ++step) {
                  double angle = (double)step / (double)RING_STEPS * Math.PI * (double)2.0F;
                  Vec3 point = new Vec3(pillar.at.x + Math.cos(angle) * radius, pillar.at.y, pillar.at.z + Math.sin(angle) * radius);
                  if (previous != null) {
                     Gizmos.line(previous, point, colour, RING_WIDTH).setAlwaysOnTop();
                  }

                  previous = point;
               }
            }
         } catch (Exception e) {
         }

      }
   }

   private static void hud(GuiGraphicsExtractor graphics, DeltaTracker tracker) {
      if (FavelaPower.on() && !FavelaCrateReel.spinning()) {
         if (Config.monolith && Config.monolithHud && !pillars.isEmpty()) {
            Minecraft client = Minecraft.getInstance();
            if (client.player != null) {
               Font font = client.font;
               graphics.pose().pushMatrix();
               graphics.pose().translate((float)Config.monolithX, (float)Config.monolithY);
               graphics.pose().scale(Config.monolithScale, Config.monolithScale);
               int row = 0;

               for(Pillar pillar : pillars) {
                  graphics.text(font, pillar.active ? "Monolith A" : "Monolith B", 0, row, pillar.active ? ACTIVE_LABEL : LABEL, true);
                  row += font.lineHeight + 1;
               }

               graphics.pose().popMatrix();
            }
         }
      }
   }

   public static String probe() {
      Minecraft client = Minecraft.getInstance();
      if (client.level != null && client.player != null) {
         StringBuilder out = new StringBuilder();
         out.append("[FA Monolith] probe\n");
         AABB search = client.player.getBoundingBox().inflate(SCAN_RANGE);
         int seen = 0;

         try {
            for(Entity entity : client.level.getEntities(client.player, search)) {
               if (bone(entity)) {
                  ++seen;
                  Vec3 at = FavelaDisplays.renderedPosition(entity);
                  ItemStack stack = ((Display.ItemDisplay)entity).getItemStack();
                  String item = stack == null || stack.isEmpty() ? "-" : FavelaDisplays.sanitize(stack.getHoverName().getString()).trim();
                  Object[] row = new Object[]{FavelaDisplays.modelId(entity), biggestScale(entity), at.distanceTo(client.player.position()), at.y, item, FavelaDisplays.modelId(stack)};
                  out.append(String.format(Locale.ROOT, "  %-48s scale %.3f  dist %5.1f  y %7.2f  item %s / %s%n", row));
               }
            }
         } catch (Exception e) {
            out.append("  failed: ").append(e.getMessage()).append("\n");
         }

         System.out.print(out);
         return seen == 0 ? "§cNo monolith bones in range." : "§aProbed §e" + seen + "§a bones, see the log.";
      } else {
         return "§cNo world.";
      }
   }
}

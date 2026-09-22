package com.favelaaddons;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class FavelaMonolith {
   private static final String ANCHOR_BONE = "/body";
   private static final double SCAN_RANGE = 48.0;
   private static final double STUCK_TO_USER = 2.5;
   private static final long ATTACK_STEP_MILLIS = 1500L;
   private static final int ATTACK_CAP = 20;
   private static final double DUAL_SCALE = 0.66;
   private static final double TEMPERED_SCALE = 0.5;
   private static final double LONE_DEFENCE = 7.5;
   private static final int CROWD_FOR_RANGE = 2;
   private static final int RING_STEPS = 48;
   private static final float RING_WIDTH = 2.0F;
   private static final int RING_LIFT = 0;
   private static final int DEFENSIVE_TINT = -16711792;
   private static final int LABEL = -1;
   private static final int DIM = -5592406;
   private static final Map<Integer, Long> born = new HashMap();
   private static final List<Pillar> pillars = new ArrayList();

   public static void registrar() {
      ClientTickEvents.END_CLIENT_TICK.register(FavelaMonolith::onTick);
      LevelRenderEvents.BEFORE_GIZMOS.register(FavelaMonolith::render);
      HudElementRegistry.addLast(Identifier.parse("favelaaddons:monolith"), FavelaMonolith::hud);
   }

   private static class Pillar {
      int id;
      Vec3 at;
      long since;
      boolean defensive;
      int crowd;
   }

   private static boolean anchor(Entity entity) {
      if (!(entity instanceof Display.ItemDisplay)) {
         return false;
      } else {
         String model = FavelaDisplays.modelId(entity);
         if (model == null) {
            return false;
         } else {
            return !FavelaDisplays.matchesAny(model, Config.parsedMonolithModels) ? false : model.toLowerCase(Locale.ROOT).endsWith(ANCHOR_BONE);
         }
      }
   }

   private static void onTick(Minecraft client) {
      if (FavelaPower.off()) {
         return;
      }

      if (!Config.monolith) {
         if (!pillars.isEmpty()) {
            pillars.clear();
            born.clear();
         }

         return;
      }

      if (client.level == null || client.player == null) {
         pillars.clear();
         born.clear();
         return;
      }

      long now = System.currentTimeMillis();
      Set<Integer> alive = new HashSet();
      List<Pillar> found = new ArrayList();
      AABB search = client.player.getBoundingBox().inflate(SCAN_RANGE);

      try {
         for(Entity entity : client.level.getEntities(client.player, search)) {
            if (anchor(entity)) {
               int id = entity.getId();
               alive.add(id);
               Long start = (Long)born.get(id);
               if (start == null) {
                  start = now;
                  born.put(id, start);
               }

               Pillar pillar = new Pillar();
               pillar.id = id;
               pillar.at = FavelaDisplays.renderedPosition(entity);
               pillar.since = start;
               pillar.defensive = pillar.at.distanceTo(client.player.position()) <= STUCK_TO_USER;
               pillar.crowd = 0;

               for(Player other : client.level.players()) {
                  if (other.position().distanceTo(pillar.at) <= (double)Config.monolithRadius) {
                     ++pillar.crowd;
                  }
               }

               found.add(pillar);
            }
         }
      } catch (Exception e) {
      }

      Iterator<Integer> stale = born.keySet().iterator();

      while(stale.hasNext()) {
         if (!alive.contains(stale.next())) {
            stale.remove();
         }
      }

      pillars.clear();
      pillars.addAll(found);
   }

   private static double effectiveness() {
      return pillars.size() >= 2 ? DUAL_SCALE : (double)1.0F;
   }

   private static double seconds(Pillar pillar) {
      return (double)(System.currentTimeMillis() - pillar.since) / (double)1000.0F;
   }

   private static double attack(Pillar pillar) {
      long steps = (System.currentTimeMillis() - pillar.since) / ATTACK_STEP_MILLIS;
      if (steps > (long)ATTACK_CAP) {
         steps = (long)ATTACK_CAP;
      }

      return (double)steps * effectiveness();
   }

   private static void render(LevelRenderContext context) {
      if (FavelaPower.off()) {
         return;
      }

      if (Config.monolith && Config.monolithRing && !pillars.isEmpty()) {
         try {
            double radius = (double)Math.max(1, Config.monolithRadius);

            for(Pillar pillar : pillars) {
               int colour = (pillar.defensive ? DEFENSIVE_TINT : Config.monolithColor) | -16777216;
               double y = pillar.at.y + (double)RING_LIFT;
               Vec3 previous = null;

               for(int step = 0; step <= RING_STEPS; ++step) {
                  double angle = (double)step / (double)RING_STEPS * Math.PI * (double)2.0F;
                  Vec3 point = new Vec3(pillar.at.x + Math.cos(angle) * radius, y, pillar.at.z + Math.sin(angle) * radius);
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

   private static String line(Pillar pillar) {
      StringBuilder out = new StringBuilder();
      out.append(pillar.defensive ? "Defensive " : "Offensive ");
      Object[] clock = new Object[]{seconds(pillar)};
      out.append(String.format(Locale.ROOT, "%.1fs", clock));
      if (pillar.defensive) {
         if (pillar.crowd > CROWD_FOR_RANGE) {
            out.append("  +1 range");
         } else if (pillar.crowd <= 1) {
            Object[] shield = new Object[]{LONE_DEFENCE * effectiveness()};
            out.append(String.format(Locale.ROOT, "  +%.1f def", shield));
         } else {
            out.append("  no bonus");
         }
      } else {
         double gained = attack(pillar);
         if (Config.monolithTempered) {
            Object[] vit = new Object[]{gained * TEMPERED_SCALE};
            out.append(String.format(Locale.ROOT, "  +%.1f vit", vit));
         } else {
            Object[] atk = new Object[]{gained};
            out.append(String.format(Locale.ROOT, "  +%.1f atk", atk));
         }
      }

      return out.toString();
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
               if (pillars.size() >= 2) {
                  graphics.text(font, "Dual Pillars 66%", 0, row, DIM, true);
                  row += font.lineHeight + 1;
               }

               for(Pillar pillar : pillars) {
                  graphics.text(font, line(pillar), 0, row, LABEL, true);
                  row += font.lineHeight + 1;
               }

               graphics.pose().popMatrix();
            }
         }
      }
   }
}

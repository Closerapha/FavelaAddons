package com.favelaaddons;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3fc;

public class FavelaMonolith {
   private static final String[] MODELS = new String[]{"arcanist_orb"};
   private static final String ANCHOR_BONE = "/body";
   private static final String GLOW_BONE = "/orb";
   private static final double SCAN_RANGE = 48.0;
   private static final double NEAR_CENTRE = 3.0;
   private static final int PLAIN_GLOW = 16777215;
   private static final int ATTACK_GLOW = 5636095;
   private static final int VITALITY_GLOW = 16733695;
   private static final int ALERT_TINT = -21846;
   private static final float ALERT_SCALE = 2.0F;
   private static final int ALERT_LIFT = 40;
   private static final List<Pillar> pillars = new ArrayList();
   private static final Map<String, EntityDataAccessor> keys = new HashMap();

   public static void registrar() {
      ClientTickEvents.END_CLIENT_TICK.register(FavelaMonolith::onTick);
      HudElementRegistry.addLast(Identifier.parse("favelaaddons:monolith"), FavelaMonolith::hud);
   }

   private static class Pillar {
      Entity anchor;
      Vec3 at;
      int glow;
   }

   private static Object hidden(Entity entity, String field) {
      try {
         EntityDataAccessor key = (EntityDataAccessor)keys.get(field);
         if (key == null) {
            java.lang.reflect.Field found = Display.class.getDeclaredField(field);
            found.setAccessible(true);
            key = (EntityDataAccessor)found.get(null);
            keys.put(field, key);
         }

         return entity.getEntityData().get(key);
      } catch (Throwable e) {
         return null;
      }
   }

   private static boolean shown(Entity entity) {
      Object range = hidden(entity, "DATA_VIEW_RANGE_ID");
      return !(range instanceof Float) ? true : ((Float)range).floatValue() > 0.0F;
   }

   private static int glowOf(Entity entity) {
      Object colour = hidden(entity, "DATA_GLOW_COLOR_OVERRIDE_ID");
      return !(colour instanceof Integer) ? PLAIN_GLOW : ((Integer)colour).intValue() & 16777215;
   }

   private static boolean bone(Entity entity) {
      if (!(entity instanceof Display.ItemDisplay)) {
         return false;
      } else {
         String model = FavelaDisplays.modelId(entity);
         return model != null && FavelaDisplays.matchesAny(model, MODELS);
      }
   }

   private static boolean named(Entity entity, String tail) {
      String model = FavelaDisplays.modelId(entity);
      return model != null && model.toLowerCase(Locale.ROOT).endsWith(tail);
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
      List<Entity> lamps = new ArrayList();
      AABB search = client.player.getBoundingBox().inflate(SCAN_RANGE);

      try {
         for(Entity entity : client.level.getEntities(client.player, search)) {
            if (bone(entity) && shown(entity)) {
               if (named(entity, ANCHOR_BONE)) {
                  anchors.add(entity);
               } else if (named(entity, GLOW_BONE) && glowOf(entity) != PLAIN_GLOW) {
                  lamps.add(entity);
               }
            }
         }
      } catch (Exception e) {
      }

      List<Pillar> found = new ArrayList();

      for(Entity anchor : anchors) {
         Vec3 at = FavelaDisplays.renderedPosition(anchor);
         Pillar pillar = new Pillar();
         pillar.anchor = anchor;
         pillar.at = at;
         pillar.glow = PLAIN_GLOW;
         double closest = Double.MAX_VALUE;

         for(Entity lamp : lamps) {
            double gap = FavelaDisplays.renderedPosition(lamp).distanceTo(at);
            if (gap < closest) {
               closest = gap;
               pillar.glow = glowOf(lamp);
            }
         }

         found.add(pillar);
      }

      pillars.clear();
      pillars.addAll(found);
   }

   public static String unit() {
      return Config.monolithBlocks ? " blocks" : "m";
   }

   public static boolean unplaced() {
      return Config.monolithDistanceX == 0 && Config.monolithDistanceY == 0;
   }

   public static int homeX(int screenWidth, int textWidth) {
      return unplaced() ? screenWidth / 2 - textWidth / 2 : Config.monolithDistanceX;
   }

   public static int homeY(int screenHeight) {
      return unplaced() ? screenHeight / 2 + 20 : Config.monolithDistanceY;
   }

   public static String wording(int glow) {
      if (glow == ATTACK_GLOW) {
         return Config.monolithAttackText;
      } else if (glow == VITALITY_GLOW) {
         return Config.monolithVitalityText;
      } else {
         return String.format(Locale.ROOT, "#%06X", new Object[]{glow});
      }
   }

   private static Pillar offensive() {
      Pillar farthest = null;
      double reach = -1.0;
      Minecraft client = Minecraft.getInstance();
      if (client.player == null) {
         return null;
      } else {
         for(Pillar pillar : pillars) {
            double gap = spanTo(client, pillar);
            if (gap > reach) {
               reach = gap;
               farthest = pillar;
            }
         }

         return farthest;
      }
   }

   private static double spanTo(Minecraft client, Pillar pillar) {
      Vec3 at = pillar.anchor == null ? pillar.at : FavelaDisplays.renderedPosition(pillar.anchor);
      return at.distanceTo(client.player.position());
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
                  graphics.text(font, wording(pillar.glow), 0, row, pillar.glow | -16777216, true);
                  row += font.lineHeight + 1;
               }

               graphics.pose().popMatrix();
            }
         }

         if (Config.monolith && !pillars.isEmpty()) {
            Minecraft client = Minecraft.getInstance();
            Pillar target = offensive();
            if (client.player != null && target != null) {
               double span = spanTo(client, target);
               Font font = client.font;
               if (Config.monolithDistance) {
                  Object[] metres = new Object[]{span};
                  String text = String.format(Locale.ROOT, "%.1f", metres) + unit();
                  float scale = Config.monolithDistanceScale <= 0.0F ? 1.0F : Config.monolithDistanceScale;
                  graphics.pose().pushMatrix();
                  graphics.pose().translate((float)homeX(graphics.guiWidth(), font.width(text)), (float)homeY(graphics.guiHeight()));
                  graphics.pose().scale(scale, scale);
                  graphics.text(font, text, 0, 0, target.glow | -16777216, true);
                  graphics.pose().popMatrix();
               }

               if (Config.monolithTeleport && span >= (double)Config.monolithTeleportAt) {
                  String warn = Config.monolithTeleportText;
                  if (warn != null && !warn.trim().isEmpty()) {
                     graphics.pose().pushMatrix();
                     graphics.pose().translate((float)(graphics.guiWidth() / 2), (float)(graphics.guiHeight() / 2 - ALERT_LIFT));
                     graphics.pose().scale(ALERT_SCALE, ALERT_SCALE);
                     graphics.text(font, warn, -font.width(warn) / 2, 0, ALERT_TINT, true);
                     graphics.pose().popMatrix();
                  }
               }
            }
         }

      }
   }

   private static String describe(Entity entity) {
      String model = FavelaDisplays.modelId(entity);
      if (model == null && entity instanceof Display.ItemDisplay) {
         ItemStack stack = ((Display.ItemDisplay)entity).getItemStack();
         if (stack != null && !stack.isEmpty()) {
            model = "item:" + FavelaDisplays.sanitize(stack.getHoverName().getString()).trim();
         }
      }

      return model == null ? entity.getType().toString() : model;
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

   private static String save(String body) {
      try {
         File folder = new File(new File(FabricLoader.getInstance().getConfigDir().toFile(), "favelaaddons"), "debug");
         if (!folder.exists() && !folder.mkdirs()) {
            return null;
         } else {
            String name = "monolith-" + (new SimpleDateFormat("yyyyMMdd-HHmmss")).format(new Date()) + ".txt";
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(new File(folder, name)), StandardCharsets.UTF_8);
            writer.write(body);
            writer.close();
            return name;
         }
      } catch (Exception e) {
         return null;
      }
   }

   public static String probe() {
      Minecraft client = Minecraft.getInstance();
      if (client.level == null || client.player == null) {
         return "§cNo world.";
      } else {
         StringBuilder out = new StringBuilder();
         out.append("[FA Monolith] probe").append(System.lineSeparator());
         List<Vec3> centres = new ArrayList();
         AABB search = client.player.getBoundingBox().inflate(SCAN_RANGE);
         int bones = 0;

         try {
            for(Entity entity : client.level.getEntities(client.player, search)) {
               if (bone(entity)) {
                  ++bones;
                  Vec3 at = FavelaDisplays.renderedPosition(entity);
                  if (named(entity, ANCHOR_BONE) && shown(entity)) {
                     centres.add(at);
                  }

                  Object[] row = new Object[]{describe(entity), biggestScale(entity), at.y, glowOf(entity), hidden(entity, "DATA_VIEW_RANGE_ID")};
                  out.append(String.format(Locale.ROOT, "  bone %-44s scale %7.3f  y %7.2f  glow #%06X  view %s%n", row));
               }
            }

            for(Entity entity : client.level.getEntities(client.player, search)) {
               if (!bone(entity)) {
                  Vec3 at = FavelaDisplays.renderedPosition(entity);

                  for(Vec3 centre : centres) {
                     if (at.distanceTo(centre) <= NEAR_CENTRE) {
                        Object[] row = new Object[]{entity.getType().toString(), describe(entity), at.distanceTo(centre), at.y};
                        out.append(String.format(Locale.ROOT, "  near %-30s %-40s off %5.2f  y %7.2f%n", row));
                        break;
                     }
                  }
               }
            }
         } catch (Exception e) {
            out.append("  failed: ").append(e.getMessage()).append(System.lineSeparator());
         }

         System.out.print(out);
         if (bones == 0) {
            return "§cNo monolith bones in range.";
         } else {
            String name = save(out.toString());
            return "§aProbed §e" + bones + "§a bones -> §e" + (name == null ? "log only" : "config/favelaaddons/debug/" + name);
         }
      }
   }
}

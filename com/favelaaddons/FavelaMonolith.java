package com.favelaaddons;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.List;
import java.util.Locale;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.syncher.EntityDataAccessor;
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
   private static final double NEAR_CENTRE = 3.0;
   private static final int RING_STEPS = 48;
   private static final float RING_WIDTH = 2.0F;
   private static final int ACTIVE_TINT = -16711792;
   private static final int LABEL = -1;
   private static final int ACTIVE_LABEL = -16711792;
   private static final List<Pillar> pillars = new ArrayList();
   private static final Map<Integer, String> marks = new HashMap();
   private static final Map<Integer, String> guests = new HashMap();
   private static final double GUEST_RANGE = 5.0;

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

   private static final Map<String, EntityDataAccessor> keys = new HashMap();

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
         return "?";
      }
   }

   private static String fingerprint(Entity entity) {
      StringBuilder out = new StringBuilder();

      try {
         ItemStack stack = ((Display.ItemDisplay)entity).getItemStack();
         if (stack == null || stack.isEmpty()) {
            out.append("empty");
         } else {
            out.append(stack.getItem());
            out.append(" model=").append(FavelaDisplays.modelId(stack));
            out.append(" parts=").append(stack.getComponents());
         }
      } catch (Exception e) {
         out.append("?");
      }

      out.append(" glow=").append(hidden(entity, "DATA_GLOW_COLOR_OVERRIDE_ID"));
      out.append(" bright=").append(hidden(entity, "DATA_BRIGHTNESS_OVERRIDE_ID"));
      out.append(" billboard=").append(hidden(entity, "DATA_BILLBOARD_RENDER_CONSTRAINTS_ID"));
      out.append(" shadow=").append(hidden(entity, "DATA_SHADOW_STRENGTH_ID"));
      out.append(" viewRange=").append(hidden(entity, "DATA_VIEW_RANGE_ID"));
      out.append(" leftRotation=").append(hidden(entity, "DATA_LEFT_ROTATION_ID"));
      out.append(" invisible=").append(entity.isInvisible());
      out.append(" glowing=").append(entity.isCurrentlyGlowing());
      return out.toString();
   }

   private static void watch(Entity entity) {
      String now = fingerprint(entity);
      String before = (String)marks.put(entity.getId(), now);
      if (before != null && !before.equals(now)) {
         System.out.println("[FA Monolith] CHANGED " + FavelaDisplays.modelId(entity));
         System.out.println("    was " + before);
         System.out.println("    now " + now);
      }
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

   private static void guestCheck(Minecraft client, List<Entity> anchors) {
      if (anchors.isEmpty()) {
         if (!guests.isEmpty()) {
            guests.clear();
         }

         return;
      }

      Map<Integer, String> now = new HashMap();

      try {
         for(Entity anchor : anchors) {
            Vec3 centre = FavelaDisplays.renderedPosition(anchor);
            AABB box = new AABB(centre.x - GUEST_RANGE, centre.y - GUEST_RANGE, centre.z - GUEST_RANGE, centre.x + GUEST_RANGE, centre.y + GUEST_RANGE, centre.z + GUEST_RANGE);

            for(Entity entity : client.level.getEntities(client.player, box)) {
               if (!bone(entity)) {
                  Vec3 at = FavelaDisplays.renderedPosition(entity);
                  if (at.distanceTo(centre) <= GUEST_RANGE) {
                     Object[] row = new Object[]{entity.getType().toString(), describe(entity), at.y - centre.y, at.distanceTo(centre)};
                     now.put(entity.getId(), String.format(Locale.ROOT, "%s %s  dy %+.2f  off %.2f", row));
                  }
               }
            }
         }
      } catch (Exception e) {
         return;
      }

      for(Integer id : now.keySet()) {
         if (!guests.containsKey(id)) {
            System.out.println("[FA Monolith] + " + id + "  " + (String)now.get(id));
         }
      }

      Set<Integer> gone = new HashSet(guests.keySet());
      gone.removeAll(now.keySet());

      for(Integer id : gone) {
         System.out.println("[FA Monolith] - " + id + "  " + (String)guests.get(id));
      }

      guests.clear();
      guests.putAll(now);
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
         out.append("[FA Monolith] probe\n");
         List<Vec3> centres = new ArrayList();
         AABB search = client.player.getBoundingBox().inflate(SCAN_RANGE);
         int bones = 0;

         try {
            for(Entity entity : client.level.getEntities(client.player, search)) {
               if (bone(entity)) {
                  ++bones;
                  Vec3 at = FavelaDisplays.renderedPosition(entity);
                  if (named(entity, ANCHOR_BONE)) {
                     centres.add(at);
                  }

                  Object[] row = new Object[]{describe(entity), biggestScale(entity), at.y};
                  out.append(String.format(Locale.ROOT, "  bone %-44s scale %7.3f  y %7.2f%n", row));
                  out.append("       ").append(fingerprint(entity)).append(System.lineSeparator());
               }
            }

            out.append("  --- within ").append(NEAR_CENTRE).append(" blocks of a monolith centre ---\n");

            for(Entity entity : client.level.getEntities(client.player, search)) {
               if (!bone(entity)) {
                  Vec3 at = FavelaDisplays.renderedPosition(entity);

                  for(Vec3 centre : centres) {
                     if (at.distanceTo(centre) <= NEAR_CENTRE) {
                        double scale = entity instanceof Display ? biggestScale(entity) : 0.0;
                        Object[] row = new Object[]{entity.getType().toString(), describe(entity), scale, at.distanceTo(centre), at.y};
                        out.append(String.format(Locale.ROOT, "  near %-30s %-40s scale %7.3f  off %5.2f  y %7.2f%n", row));
                        break;
                     }
                  }
               }
            }
         } catch (Exception e) {
            out.append("  failed: ").append(e.getMessage()).append("\n");
         }

         System.out.print(out);
         if (bones == 0) {
            return "§cNo monolith bones in range.";
         } else {
            String name = save(out.toString());
            return "§aProbed §e" + bones + "§a bones, §e" + centres.size() + "§a centre(s) -> §e" + (name == null ? "log only" : "config/favelaaddons/debug/" + name);
         }
      }
   }
}

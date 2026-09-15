package com.sapo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Interaction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3fc;

public class SapoDebugger {
   private static final double CROSSHAIR_TOLERANCE = 0.5;

   public static void dumpEntities(Minecraft client) {
      if (client.level != null && client.player != null) {
         double radius = (double)Math.max(1, Config.debuggerRadius);
         boolean crosshair = Config.debuggerCrosshair;
         List<Entity> nearby = crosshair ? collectLookedAt(client, radius) : collectAround(client, radius);
         nearby.sort(Comparator.comparingDouble((entity) -> (double)entity.distanceTo(client.player)));
         String header = String.format(Locale.ROOT, "Player: %.2f / %.2f / %.2f | Mode: %s | Range: %.0f blocks | Entities: %d", client.player.getX(), client.player.getY(), client.player.getZ(), crosshair ? "crosshair" : "radius", radius, nearby.size());
         Map<String, Integer> counts = new LinkedHashMap();
         Map<String, Cluster> clusters = new LinkedHashMap();
         StringBuilder details = new StringBuilder();
         int index = 0;

         for(Entity entity : nearby) {
            ++index;
            String typeId = String.valueOf(EntityType.getKey(entity.getType()));
            counts.merge(typeId, 1, Integer::sum);
            String identity = identityOf(entity, typeId);
            ((Cluster)clusters.computeIfAbsent(identity, (key) -> new Cluster())).add(entity, SapoDisplays.renderedPosition(entity), client);
            details.append(String.format(Locale.ROOT, "#%-3d %6.2fb  %-34s id=%-9d pos=(%.2f, %.2f, %.2f)", index, entity.distanceTo(client.player), typeId, entity.getId(), entity.getX(), entity.getY(), entity.getZ()));
            if (entity instanceof LivingEntity) {
               LivingEntity living = (LivingEntity)entity;
               details.append(String.format(Locale.ROOT, " hp=%.1f/%.1f", living.getHealth(), living.getMaxHealth()));
               details.append(SapoGear.describe(entity));
            }

            if (entity.isInvisible()) {
               details.append(" [invisible]");
            }

            if (entity.getVehicle() != null) {
               details.append(" [riding id=").append(entity.getVehicle().getId()).append("]");
            }

            AABB box = entity.getBoundingBox();
            details.append(String.format(Locale.ROOT, "\n        size: %.2f x %.2f x %.2f", box.getXsize(), box.getYsize(), box.getZsize()));
            String name = readName(entity);
            if (name != null) {
               details.append("\n        name: \"").append(name).append("\"");
               if (SapoGlyphs.isGlyphText(name)) {
                  details.append("\n        text: \"").append(SapoGlyphs.decode(name)).append("\"");
               }

               if (Config.devMode) {
                  details.append("\n        raw : ").append(readRawName(entity));
               }
            }

            appendDisplayDetails(entity, details);
            appendItemDetails(entity, details);
            details.append("\n");
         }

         StringBuilder summary = new StringBuilder();
         summary.append("========== [FA Debugger] ==========\n");
         summary.append(header).append("\n");
         if (nearby.isEmpty()) {
            summary.append("(no entities found in the radius)\n");
         } else {
            summary.append("--- Types ---\n");

            for(Map.Entry<String, Integer> entry : counts.entrySet()) {
               summary.append(String.format(Locale.ROOT, "%-34s x%d%n", entry.getKey(), entry.getValue()));
            }

            summary.append("--- Identities (rendered position: distance, height, footprint) ---\n");

            for(Map.Entry<String, Cluster> entry : clusters.entrySet()) {
               Cluster cluster = (Cluster)entry.getValue();
               summary.append(String.format(Locale.ROOT, "%-62s x%-4d dist %5.1f-%5.1f  y %6.2f-%6.2f  span %5.1f x %5.1f%n", entry.getKey(), cluster.count, cluster.minDist, cluster.maxDist, cluster.minY, cluster.maxY, cluster.maxX - cluster.minX, cluster.maxZ - cluster.minZ));
            }
         }

         summary.append("--- Boss bars ---\n");
         int bars = 0;
         net.minecraft.client.gui.components.LerpingBossEvent active = SapoBossHp.activeBar();

         for(net.minecraft.client.gui.components.LerpingBossEvent bar : SapoCalls.bossBars()) {
            ++bars;
            summary.append(String.format(Locale.ROOT, "#%d %6.2f%%%s  name=\"%s\"%n", bars, bar.getProgress() * 100.0F, bar == active ? " <= tracked" : "", bar.getName().getString()));
            summary.append("      raw: ").append(bar.getName()).append("\n");
         }

         if (bars == 0) {
            summary.append("(no vanilla boss bar on screen)\n");
         }

         summary.append("--- Auto Walls ---\n");
         summary.append(SapoAutoWalls.diagnose(client)).append("\n");
         summary.append("=====================================");
         String summaryText = summary.toString();
         System.out.println(summaryText);
         String fileName = writeDumpFile(summaryText, details.toString());

         try {
            client.keyboardHandler.setClipboard(summaryText);
         } catch (Exception var20) {
         }

         String where = crosshair ? " entities in the crosshair line" : " entities within " + (int)radius + " blocks";
         String saved = fileName != null ? " §7- summary copied, full dump in config/sapo/debug/" + fileName : " §7- summary copied to clipboard";
         client.player.sendSystemMessage(Component.literal("§a[FA Debug] §f" + nearby.size() + where + saved));
      }
   }

   private static String writeDumpFile(String summary, String details) {
      try {
         File folder = new File(new File(FabricLoader.getInstance().getConfigDir().toFile(), "sapo"), "debug");
         if (!folder.exists() && !folder.mkdirs()) {
            return null;
         } else {
            String fileName = "dump-" + (new SimpleDateFormat("yyyyMMdd-HHmmss")).format(new Date()) + ".txt";
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(new File(folder, fileName)), StandardCharsets.UTF_8);

            try {
               writer.write(summary);
               writer.write("\n\n--- Entities ---\n");
               writer.write(details);
            } catch (Throwable var7) {
               try {
                  writer.close();
               } catch (Throwable var6) {
                  var7.addSuppressed(var6);
               }

               throw var7;
            }

            writer.close();
            return fileName;
         }
      } catch (Exception e) {
         System.out.println("[FA Debug] Could not write dump file: " + e.getMessage());
         return null;
      }
   }

   public static String identityOf(Entity entity, String typeId) {
      if (entity instanceof Display.ItemDisplay) {
         ItemStack stack = ((Display.ItemDisplay)entity).getItemStack();
         if (stack == null || stack.isEmpty()) {
            return typeId + " - empty";
         } else {
            Object model = stack.get(DataComponents.ITEM_MODEL);
            return model != null ? String.valueOf(model) : typeId + " - " + readItemId(stack);
         }
      } else if (entity instanceof Interaction) {
         Interaction interaction = (Interaction)entity;
         return String.format(Locale.ROOT, "%s - %.2fx%.2f%s", typeId, interaction.getWidth(), interaction.getHeight(), interaction.getResponse() ? " clickable" : "");
      } else if (entity instanceof Display.BlockDisplay) {
         return typeId + " - " + ((Display.BlockDisplay)entity).getBlockState();
      } else {
         String gear = SapoGear.model(entity);
         String label = labelOf(entity);
         String base = gear.isEmpty() ? typeId : typeId + " [" + gear + "]";
         return label.isEmpty() ? base : base + " - \"" + label + "\"";
      }
   }

   private static String labelOf(Entity entity) {
      String name = readName(entity);
      if (name == null) {
         return "";
      } else {
         String clean = SapoGlyphs.isGlyphText(name) ? SapoGlyphs.decode(name) : SapoAutoClicker.sanitize(name).trim();
         if (clean.isEmpty()) {
            return "";
         } else {
            return clean.length() > 40 ? clean.substring(0, 40) + "..." : clean;
         }
      }
   }

   private static void appendItemDetails(Entity entity, StringBuilder details) {
      if (entity instanceof Display.ItemDisplay) {
         ItemStack stack = ((Display.ItemDisplay)entity).getItemStack();
         if (stack != null && !stack.isEmpty()) {
            details.append("\n        item: ").append(stack.getCount()).append("x ").append(readItemId(stack));
            Object model = stack.get(DataComponents.ITEM_MODEL);
            if (model != null) {
               details.append("\n        item model: ").append(model);
            }

            Object modelData = stack.get(DataComponents.CUSTOM_MODEL_DATA);
            if (modelData != null) {
               details.append("\n        custom model data: ").append(modelData);
            }

            Object customData = stack.get(DataComponents.CUSTOM_DATA);
            if (customData != null) {
               details.append("\n        custom data: ").append(customData);
            }

            if (Config.debuggerComponents) {
               details.append("\n        components: ").append(stack.getComponents());
            }
         }
      } else if (entity instanceof Display.BlockDisplay) {
         details.append("\n        block: ").append(((Display.BlockDisplay)entity).getBlockState());
      }

   }



   private static void appendDisplayDetails(Entity entity, StringBuilder details) {
      if (entity instanceof Display) {
         try {
            Vector3fc translation = (Vector3fc)entity.getEntityData().get(Display.DATA_TRANSLATION_ID);
            Vector3fc scale = (Vector3fc)entity.getEntityData().get(Display.DATA_SCALE_ID);
            if (translation != null) {
               details.append(String.format(Locale.ROOT, "\n        translation: (%.2f, %.2f, %.2f)", translation.x(), translation.y(), translation.z()));
               Vec3 rendered = SapoDisplays.renderedPosition(entity);
               details.append(String.format(Locale.ROOT, "\n        rendered at: (%.2f, %.2f, %.2f)  yaw=%.1f", rendered.x, rendered.y, rendered.z, entity.getYRot()));
            }

            if (scale != null) {
               details.append(String.format(Locale.ROOT, "\n        scale: (%.2f, %.2f, %.2f)", scale.x(), scale.y(), scale.z()));
            }
         } catch (Exception e) {
            details.append("\n        transformation: unavailable (").append(e.getMessage()).append(")");
         }
      }

   }

   private static String readItemId(ItemStack stack) {
      try {
         return String.valueOf(stack.getItem().builtInRegistryHolder().key().identifier());
      } catch (Exception e) {
         return stack.getItem().getDescriptionId();
      }
   }

   private static List<Entity> collectAround(Minecraft client, double radius) {
      List<Entity> found = new ArrayList();
      AABB box = client.player.getBoundingBox().inflate(radius);

      for(Entity entity : client.level.getEntities(client.player, box)) {
         if ((double)entity.distanceTo(client.player) <= radius) {
            found.add(entity);
         }
      }

      return found;
   }

   private static List<Entity> collectLookedAt(Minecraft client, double range) {
      List<Entity> found = new ArrayList();
      Vec3 eye = client.player.getEyePosition();
      Vec3 reach = client.player.getViewVector(1.0F).scale(range);
      Vec3 end = eye.add(reach);
      AABB box = client.player.getBoundingBox().expandTowards(reach).inflate(CROSSHAIR_TOLERANCE);

      for(Entity entity : client.level.getEntities(client.player, box)) {
         AABB hitbox = markerBox(entity).inflate(CROSSHAIR_TOLERANCE);
         if (hitbox.clip(eye, end).isPresent()) {
            found.add(entity);
         }
      }

      return found;
   }

   private static AABB markerBox(Entity entity) {
      AABB box = entity.getBoundingBox();
      return box.getXsize() >= 0.1 && box.getYsize() >= 0.1 && box.getZsize() >= 0.1 ? box : new AABB(entity.getX(), entity.getY(), entity.getZ(), entity.getX(), entity.getY(), entity.getZ());
   }

   private static String readName(Entity entity) {
      if (entity instanceof Display.TextDisplay) {
         Component text = ((Display.TextDisplay)entity).getText();
         return text != null ? text.getString() : null;
      } else if (entity instanceof ArmorStand) {
         ArmorStand stand = (ArmorStand)entity;
         return stand.hasCustomName() ? stand.getCustomName().getString() : null;
      } else if (entity.hasCustomName()) {
         return entity.getCustomName().getString();
      } else {
         return entity.getType() == EntityType.PLAYER ? entity.getName().getString() : null;
      }
   }

   private static String readRawName(Entity entity) {
      if (entity instanceof Display.TextDisplay) {
         Component text = ((Display.TextDisplay)entity).getText();
         return text != null ? text.toString() : "null";
      } else if (entity instanceof ArmorStand && ((ArmorStand)entity).hasCustomName()) {
         return ((ArmorStand)entity).getCustomName().toString();
      } else {
         return entity.hasCustomName() ? entity.getCustomName().toString() : entity.getName().toString();
      }
   }

   private static class Cluster {
      int count = 0;
      double minDist = Double.MAX_VALUE;
      double maxDist = -Double.MAX_VALUE;
      double minY = Double.MAX_VALUE;
      double maxY = -Double.MAX_VALUE;
      double minX = Double.MAX_VALUE;
      double maxX = -Double.MAX_VALUE;
      double minZ = Double.MAX_VALUE;
      double maxZ = -Double.MAX_VALUE;

      void add(Entity entity, Vec3 rendered, Minecraft client) {
         ++this.count;
         double dist = client.player.position().distanceTo(rendered);
         this.minDist = Math.min(this.minDist, dist);
         this.maxDist = Math.max(this.maxDist, dist);
         this.minY = Math.min(this.minY, rendered.y);
         this.maxY = Math.max(this.maxY, rendered.y);
         this.minX = Math.min(this.minX, rendered.x);
         this.maxX = Math.max(this.maxX, rendered.x);
         this.minZ = Math.min(this.minZ, rendered.z);
         this.maxZ = Math.max(this.maxZ, rendered.z);
      }
   }
}

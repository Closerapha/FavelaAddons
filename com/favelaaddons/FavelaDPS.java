package com.favelaaddons;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.AABB;

public class FavelaDPS {
   private static final double SCAN_RANGE = 15.0;
   private static final int MAX_REMEMBERED = 4096;
   private static final long COMBAT_GAP = 5000L;
   private static final long WINDOW_MILLIS = 3000L;
   private static final long REFRESH_MILLIS = 250L;
   private static final double MAX_HIT = 1000000.0;
   private static final char HEART = '❤';
   private static final Set<Integer> processedEntities = new HashSet();
   private static final List<DamageHit> recentHits = new ArrayList();
   public static long lastHitTime = 0L;
   public static long combatStartTime = 0L;
   private static double displayDps = 0.0;
   private static long lastDpsUpdateTime = 0L;
   private static ClientLevel lastLevel = null;
   private static int lastRead = 0;
   private static int lastSkipped = 0;

   public static void onTick(Minecraft client) {
      if (Config.dpsHudEnabled && client.level != null && client.player != null) {
         if (client.level != lastLevel) {
            lastLevel = client.level;
            forget();
         }

         if (client.level.getGameTime() % 100L == 0L) {
            processedEntities.removeIf((id) -> client.level.getEntity(id) == null);
         }

         if (processedEntities.size() > MAX_REMEMBERED) {
            processedEntities.clear();
         }

         AABB box = client.player.getBoundingBox().inflate(SCAN_RANGE);

         for(Entity entity : client.level.getEntities(client.player, box)) {
            if (!processedEntities.contains(entity.getId())) {
               Component text = textOf(entity);
               if (text != null) {
                  String plain = text.getString();
                  if (!plain.trim().isEmpty()) {
                     processedEntities.add(entity.getId());
                     if (!plain.contains("/") && !plain.contains("%") && plain.indexOf(HEART) < 0) {
                        double damage = readDamage(text, plain);
                        if (damage > 0.0 && damage < MAX_HIT) {
                           ++lastRead;
                           registerDamage(damage);

                           if (Config.hideDamageNumbers) {
                              entity.discard();
                           }
                        } else {
                           ++lastSkipped;
                        }
                     }
                  }
               }
            }
         }

      }
   }

   private static Component textOf(Entity entity) {
      if (entity instanceof Display.TextDisplay) {
         return ((Display.TextDisplay)entity).getText();
      } else {
         return entity instanceof ArmorStand && entity.hasCustomName() ? entity.getCustomName() : null;
      }
   }

   public static double readDamage(Component text, String plain) {
      double fromFont = parseNumber(decodeGlyphs(text));
      if (fromFont > 0.0) {
         return fromFont;
      } else {
         double fromRanges = parseNumber(parseTelosDamage(plain));
         return fromRanges > 0.0 ? fromRanges : parseNumber(plain.trim());
      }
   }

   public static String decodeGlyphs(Component text) {
      if (text == null) {
         return "";
      } else {
         StringBuilder out = new StringBuilder();
         text.visit((style, content) -> {
            String font = FavelaFonts.fontName(style);

            for(int glyph : content.codePoints().toArray()) {
               String texture = FavelaFonts.texture(font, glyph);
               if (texture != null) {
                  String name = texture.substring(texture.lastIndexOf(47) + 1).replace(".png", "").toLowerCase(Locale.ROOT);
                  if (name.length() == 1 && name.charAt(0) >= '0' && name.charAt(0) <= '9') {
                     out.append(name.charAt(0));
                  } else if (name.equals("dot") || name.equals("period") || name.equals("comma")) {
                     out.append('.');
                  }
               }
            }

            return Optional.empty();
         }, Style.EMPTY);
         return out.toString();
      }
   }

   public static double parseNumber(String digits) {
      String clean = digits == null ? "" : digits.trim();
      if (clean.isEmpty()) {
         return 0.0;
      } else {
         int dots = 0;

         for(int i = 0; i < clean.length(); ++i) {
            char c = clean.charAt(i);
            if (c == '.') {
               ++dots;
            } else if (c < '0' || c > '9') {
               return 0.0;
            }
         }

         if (dots > 1) {
            return 0.0;
         } else {
            try {
               return Double.parseDouble(clean);
            } catch (Exception var5) {
               return 0.0;
            }
         }
      }
   }

   public static String parseTelosDamage(String plainText) {
      StringBuilder sb = new StringBuilder();

      for(int i = 0; i < plainText.length(); ++i) {
         char c = plainText.charAt(i);
         if (c == '\ud818' && i + 1 < plainText.length()) {
            char next = plainText.charAt(i + 1);
            if (next >= '\udc25' && next <= '\udc2e') {
               sb.append((char)(48 + (next - '\udc25')));
            } else if (next >= '\udc00' && next <= '\udc03') {
               sb.append((char)(54 + (next - '\udc00')));
            } else if (next == '\udc4f' || next == '\udc4e') {
               sb.append('.');
            }

            ++i;
         } else if (c == '\ud817' && i + 1 < plainText.length()) {
            char next = plainText.charAt(i + 1);
            if (next >= '\udffa' && next <= '\udfff') {
               sb.append((char)(48 + (next - '\udffa')));
            }

            ++i;
         }
      }

      return sb.toString();
   }

   public static void registerDamage(double damage) {
      long now = System.currentTimeMillis();
      if (now - lastHitTime > COMBAT_GAP || recentHits.isEmpty()) {
         recentHits.clear();
         combatStartTime = now;
      }

      recentHits.add(new DamageHit(damage, now));
      lastHitTime = now;
   }

   public static void forget() {
      processedEntities.clear();
      recentHits.clear();
      displayDps = 0.0;
      lastHitTime = 0L;
      combatStartTime = 0L;
   }

   public static String diagnose() {
      return "remembered=" + processedEntities.size() + " hits=" + recentHits.size() + " read=" + lastRead + " skipped=" + lastSkipped + " dps=" + String.format(Locale.ROOT, "%.1f", displayDps);
   }

   public static double getCurrentDPS() {
      long now = System.currentTimeMillis();
      if (now - lastHitTime > COMBAT_GAP) {
         recentHits.clear();
         displayDps = 0.0;
         return 0.0;
      } else {
         if (now - lastDpsUpdateTime > REFRESH_MILLIS) {
            Iterator<DamageHit> it = recentHits.iterator();

            while(it.hasNext()) {
               DamageHit hit = (DamageHit)it.next();
               if (now - hit.time > WINDOW_MILLIS) {
                  it.remove();
               }
            }

            if (recentHits.isEmpty()) {
               displayDps = 0.0;
            } else {
               double sum = 0.0;

               for(DamageHit hit : recentHits) {
                  sum += hit.amount;
               }

               double combatDuration = (double)(now - combatStartTime) / 1000.0;
               double window = Math.min((double)WINDOW_MILLIS / 1000.0, combatDuration);
               if (window < 0.5) {
                  window = 0.5;
               }

               displayDps = sum / window;
            }

            lastDpsUpdateTime = now;
         }

         return displayDps;
      }
   }

   private static class DamageHit {
      final double amount;
      final long time;

      DamageHit(double amount, long time) {
         this.amount = amount;
         this.time = time;
      }
   }
}

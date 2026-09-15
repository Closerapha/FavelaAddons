package com.favelaaddons;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.AABB;

public class FavelaDPS {
   private static final Set<Integer> processedEntities = new HashSet();
   private static final List<DamageHit> recentHits = new ArrayList();
   public static long lastHitTime = 0L;
   public static long combatStartTime = 0L;
   private static double displayDps = (double)0.0F;
   private static long lastDpsUpdateTime = 0L;

   public static void onTick(Minecraft client) {
      if (Config.active && Config.dpsHudEnabled && client.level != null && client.player != null) {
         if (client.level.getGameTime() % 100L == 0L) {
            processedEntities.removeIf((id) -> client.level.getEntity(id) == null);
         }

         AABB box = client.player.getBoundingBox().inflate((double)15.0F);

         for(Entity entity : client.level.getEntities(client.player, box)) {
            if (!processedEntities.contains(entity.getId())) {
               String rawText = null;
               String plainText = null;
               if (entity instanceof Display.TextDisplay) {
                  Display.TextDisplay textDisplay = (Display.TextDisplay)entity;
                  if (textDisplay.getText() != null) {
                     rawText = textDisplay.getText().toString();
                     plainText = textDisplay.getText().getString();
                  }
               } else if (entity instanceof ArmorStand) {
                  ArmorStand stand = (ArmorStand)entity;
                  if (stand.hasCustomName()) {
                     rawText = stand.getCustomName().toString();
                     plainText = stand.getCustomName().getString();
                  }
               }

               if (rawText != null && !rawText.equals("empty") && !plainText.trim().isEmpty()) {
                  processedEntities.add(entity.getId());
                  if (Config.devMode) {
                     StringBuilder hex = new StringBuilder();

                     for(char c : plainText.toCharArray()) {
                        hex.append(String.format("\\u%04X ", (int)c));
                     }

                     System.out.println("[FA DPS Debug] New Entity Text: " + plainText);
                     System.out.println("[FA DPS Debug] Hex: " + hex.toString().trim());
                  }

                  if (!plainText.contains("/") && !plainText.contains("%") && !plainText.contains("❤") && !plainText.contains("❤")) {
                     double damage = parseTelosDamage(plainText);
                     if (damage > (double)0.0F && damage < (double)1000000.0F) {
                        registerDamage(damage);
                        if (Config.devMode) {
                           System.out.println("[FA DPS Debug] Parsed Custom Damage: " + damage);
                        }

                        if (Config.hideDamageNumbers) {
                           entity.discard();
                        }
                     } else if (damage == (double)0.0F) {
                        boolean hasLetters = false;
                        boolean hasDigits = false;

                        for(char c : plainText.toCharArray()) {
                           if (Character.isLetter(c)) {
                              hasLetters = true;
                           }

                           if (Character.isDigit(c)) {
                              hasDigits = true;
                           }
                        }

                        if (hasDigits && !hasLetters) {
                           try {
                              String numStr = plainText.replaceAll("[^0-9.]", "");
                              if (!numStr.isEmpty()) {
                                 double normalDamage = Double.parseDouble(numStr);
                                 if (normalDamage > (double)0.0F && normalDamage < (double)1000000.0F) {
                                    registerDamage(normalDamage);
                                    if (Config.devMode) {
                                       System.out.println("[FA DPS Debug] Parsed Normal Damage: " + normalDamage);
                                    }

                                    if (Config.hideDamageNumbers) {
                                       entity.discard();
                                    }
                                 }
                              }
                           } catch (Exception var14) {
                           }
                        }
                     }
                  }
               }
            }
         }

      }
   }

   public static double parseTelosDamage(String plainText) {
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

      if (sb.length() > 0) {
         try {
            return Double.parseDouble(sb.toString());
         } catch (Exception var5) {
            return (double)0.0F;
         }
      } else {
         return (double)0.0F;
      }
   }

   public static void registerDamage(double damage) {
      long now = System.currentTimeMillis();
      if (now - lastHitTime > 5000L) {
         recentHits.clear();
         combatStartTime = now;
      }

      if (recentHits.isEmpty()) {
         combatStartTime = now;
      }

      recentHits.add(new DamageHit(damage, now));
      lastHitTime = now;
   }

   public static double getCurrentDPS() {
      long now = System.currentTimeMillis();
      if (now - lastHitTime > 5000L) {
         recentHits.clear();
         displayDps = (double)0.0F;
         return (double)0.0F;
      } else {
         if (now - lastDpsUpdateTime > 250L) {
            Iterator<DamageHit> it = recentHits.iterator();

            while(it.hasNext()) {
               DamageHit hit = (DamageHit)it.next();
               if (now - hit.time > 3000L) {
                  it.remove();
               }
            }

            if (recentHits.isEmpty()) {
               displayDps = (double)0.0F;
            } else {
               double sum = (double)0.0F;

               for(DamageHit hit : recentHits) {
                  sum += hit.amount;
               }

               double combatDuration = (double)(now - combatStartTime) / (double)1000.0F;
               double window = Math.min((double)3.0F, combatDuration);
               if (window < (double)0.5F) {
                  window = (double)0.5F;
               }

               displayDps = sum / window;
            }

            lastDpsUpdateTime = now;
         }

         return displayDps;
      }
   }

   private static class DamageHit {
      double amount;
      long time;

      DamageHit(double amount, long time) {
         this.amount = amount;
         this.time = time;
      }
   }
}

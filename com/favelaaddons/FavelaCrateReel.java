package com.favelaaddons;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public class FavelaCrateReel {
   private static final String[][] CELL_ART = new String[][]{
      {"common_crate", "common"}, {"usual_pet", "common"}, {"usual_mount", "common"},
      {"uncommon_crate", "uncommon"}, {"strange_pet", "uncommon"}, {"strange_mount", "uncommon"},
      {"rare_crate", "rare"}, {"fabled_pet", "rare"}, {"fabled_mount", "rare"},
      {"epic_crate", "epic"}, {"exotic_pet", "epic"}, {"exotic_mount", "epic"},
      {"legendary_crate", "legendary"},
      {"seasonal_crate", "seasonal"}
   };
   private static final int CELL_W = 38;
   private static final int CELL_H = 52;
   private static final int FRAME_MID = 29;
   private static final int STRIP = 512;
   private static final int PITCH = 44;
   private static final float FREE_SPEED = 0.77F;
   private static final int MIN_LAND = 700;
   private static final int MAX_LAND = 1600;
   private static final long HOLD_MILLIS = 2500L;
   private static final long FREE_LIMIT = 20000L;
   private static final int MIN_AHEAD = 9;
   private static final long FADE_IN = 300L;
   private static final long FADE_OUT = 700L;
   private static final long REEL_FADE = 350L;
   private static final long BLIND_LIMIT = 8000L;
   private static final float SCALE = 2.0F;
   private static final int ROW_HEIGHT = 34;
   private static final int BACKDROP = -1777726976;
   private static final int EDGE = -14671840;
   private static final int MARKER = -1146130;
   private static final int LABEL = -1;
   private static final int IDLE = 0;
   private static final int FREE = 1;
   private static final int LANDING = 2;
   private static final List<ItemStack> reel = new ArrayList();
   private static final Random shuffle = new Random();
   private static int phase = IDLE;
   private static long freeStart = 0L;
   private static long landStart = 0L;
   private static long landMillis = 0L;
   private static float landFrom = 0.0F;
   private static float landTo = 0.0F;
   private static int landIndex = -1;
   private static String prize = "";
   private static Identifier cell = null;

   public static void registrar() {
      HudElementRegistry.addLast(Identifier.parse("favelaaddons:crate_reel"), FavelaCrateReel::render);
   }

   private static boolean drawable(ItemStack stack) {
      String model = FavelaDisplays.modelId(stack);
      return model == null || !model.endsWith(":air");
   }

   public static boolean spinning() {
      return phase != IDLE;
   }

   public static Identifier cellFor(String crate) {
      if (crate != null) {
         for(String[] entry : CELL_ART) {
            if (entry[0].equals(crate)) {
               return Identifier.parse("favelaaddons:textures/gui/crate_cell_" + entry[1] + ".png");
            }
         }
      }

      return null;
   }

   public static void begin(List<ItemStack> pool, Identifier art) {
      cell = art;
      begin(pool);
   }

   public static void begin(List<ItemStack> pool) {
      List<ItemStack> filler = new ArrayList();

      for(ItemStack stack : pool) {
         if (stack != null && !stack.isEmpty() && drawable(stack)) {
            filler.add(stack);
         }
      }

      if (!filler.isEmpty()) {
         reel.clear();

         for(int i = 0; i < STRIP; ++i) {
            reel.add(filler.get(shuffle.nextInt(filler.size())));
         }

         prize = "";
         phase = FREE;
         freeStart = System.currentTimeMillis();
      }
   }

   public static void land(ItemStack winner) {
      if (phase == FREE && winner != null && !winner.isEmpty()) {
         float here = offset();
         int target = (int)Math.ceil((double)(here / (float)PITCH)) + MIN_AHEAD;
         if (target >= reel.size()) {
            target = reel.size() - 1;
         }

         reel.set(target, winner);
         landIndex = target;
         landFrom = here;
         landTo = (float)(target * PITCH);
         long fitted = Math.round(2.0 * (double)(landTo - landFrom) / (double)FREE_SPEED);
         landMillis = Math.max((long)MIN_LAND, Math.min((long)MAX_LAND, fitted));
         prize = FavelaDisplays.sanitize(winner.getHoverName().getString()).trim();
         phase = LANDING;
         landStart = System.currentTimeMillis();
      }
   }

   public static boolean correct(ItemStack winner) {
      if (phase != LANDING) {
         return false;
      } else if (landIndex < 0 || landIndex >= reel.size()) {
         return false;
      } else if (winner != null && !winner.isEmpty()) {
         if (System.currentTimeMillis() - landStart >= landMillis) {
            return false;
         } else {
            reel.set(landIndex, winner);
            prize = FavelaDisplays.sanitize(winner.getHoverName().getString()).trim();
            return true;
         }
      } else {
         return false;
      }
   }

   public static void spin(List<ItemStack> pool, ItemStack winner) {
      land(winner);
   }

   public static void spin(List<ItemStack> pool, ItemStack winner, Identifier art) {
      begin(pool, art);
      land(winner);
   }

   private static long tail(long now) {
      return phase != LANDING ? -1L : now - landStart - landMillis - HOLD_MILLIS;
   }

   private static float hide(long now) {
      long over = tail(now);
      if (over <= 0L) {
         return 0.0F;
      } else {
         return over >= REEL_FADE ? 1.0F : (float)over / (float)REEL_FADE;
      }
   }

   private static float shade(long now) {
      float level = 1.0F;
      long since = now - freeStart;
      if (since < FADE_IN) {
         level = (float)since / (float)FADE_IN;
      }

      if (phase == FREE) {
         long over = since - BLIND_LIMIT;
         if (over > 0L) {
            float relief = over >= FADE_OUT ? 0.0F : 1.0F - (float)over / (float)FADE_OUT;
            if (relief < level) {
               level = relief;
            }
         }
      }

      long past = tail(now) - REEL_FADE;
      if (past > 0L) {
         float out = past >= FADE_OUT ? 0.0F : 1.0F - (float)past / (float)FADE_OUT;
         if (out < level) {
            level = out;
         }
      }

      return level;
   }

   private static float offset() {
      long now = System.currentTimeMillis();
      if (phase == FREE) {
         return (float)(now - freeStart) * FREE_SPEED;
      } else if (phase != LANDING) {
         return 0.0F;
      } else {
         long elapsed = now - landStart;
         if (elapsed >= landMillis) {
            return landTo;
         } else {
            float t = (float)elapsed / (float)landMillis;
            float left = 1.0F - t;
            float eased = 1.0F - left * left;
            return landFrom + (landTo - landFrom) * eased;
         }
      }
   }

   private static void render(GuiGraphicsExtractor graphics, DeltaTracker tracker) {
      Minecraft client = Minecraft.getInstance();
      if (FavelaPower.on() && phase != IDLE && !reel.isEmpty() && client.player != null) {
         long now = System.currentTimeMillis();
         if (phase == FREE && now - freeStart > FREE_LIMIT) {
            phase = IDLE;
            reel.clear();
         } else if (phase == LANDING && now - landStart > landMillis + HOLD_MILLIS + REEL_FADE + FADE_OUT) {
            phase = IDLE;
            reel.clear();
         } else if (tail(now) >= REEL_FADE) {
            int fade = Math.round(shade(now) * 255.0F);
            if (fade > 0) {
               graphics.fill(0, 0, graphics.guiWidth(), graphics.guiHeight(), fade << 24);
            }

         } else {
            int width = graphics.guiWidth();
            int height = graphics.guiHeight();
            int veil = Math.round(shade(now) * 255.0F);
            if (veil > 0) {
               graphics.fill(0, 0, width, height, veil << 24);
            }

            int centre = width / 2;
            int band = cell == null ? ROW_HEIGHT : CELL_H;
            int top = height / 2 - band;
            int anchor = cell == null ? band / 2 : FRAME_MID;
            float scroll = offset();
            if (cell == null) {
               graphics.fill(0, top, width, top + ROW_HEIGHT, BACKDROP);
               graphics.fill(0, top, width, top + 1, EDGE);
               graphics.fill(0, top + ROW_HEIGHT - 1, width, top + ROW_HEIGHT, EDGE);
            }

            graphics.pose().pushMatrix();
            graphics.pose().translate((float)centre, (float)(top + anchor));
            graphics.pose().scale(SCALE, SCALE);
            int first = Math.max(0, (int)((scroll - (float)width) / (float)PITCH));
            int last = Math.min(reel.size() - 1, (int)((scroll + (float)width) / (float)PITCH));

            for(int i = first; i <= last; ++i) {
               float x = (float)(i * PITCH) - scroll;
               int at = Math.round(x);
               if (cell != null) {
                  graphics.blit(RenderPipelines.GUI_TEXTURED, cell, at - CELL_W / 2, -FRAME_MID, 0.0F, 0.0F, CELL_W, CELL_H, CELL_W, CELL_H);
               }

               graphics.item(reel.get(i), at - 8, -8);
            }

            graphics.pose().popMatrix();
            if (cell == null) {
               graphics.fill(centre - 1, top + 1, centre + 1, top + ROW_HEIGHT - 1, MARKER);
            }

            if (phase == LANDING && now - landStart >= landMillis && !prize.isEmpty()) {
               Font font = client.font;
               graphics.text(font, prize, centre - font.width(prize) / 2, top + band + 4, LABEL, true);
            }

            int fog = Math.round(hide(now) * 255.0F);
            if (fog > 0) {
               graphics.fill(0, 0, width, height, fog << 24);
            }

         }
      }
   }
}

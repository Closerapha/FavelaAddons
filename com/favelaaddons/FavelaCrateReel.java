package com.favelaaddons;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public class FavelaCrateReel {
   private static final int STRIP = 512;
   private static final int PITCH = 24;
   private static final float FREE_SPEED = 0.34F;
   private static final long LAND_MILLIS = 3200L;
   private static final long HOLD_MILLIS = 2600L;
   private static final long FREE_LIMIT = 20000L;
   private static final int MIN_AHEAD = 14;
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
   private static float landFrom = 0.0F;
   private static float landTo = 0.0F;
   private static String prize = "";

   public static void registrar() {
      HudElementRegistry.addLast(Identifier.parse("favelaaddons:crate_reel"), FavelaCrateReel::render);
   }

   public static boolean spinning() {
      return phase != IDLE;
   }

   public static void begin(List<ItemStack> pool) {
      List<ItemStack> filler = new ArrayList();

      for(ItemStack stack : pool) {
         if (stack != null && !stack.isEmpty()) {
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
         landFrom = here;
         landTo = (float)(target * PITCH);
         prize = FavelaDisplays.sanitize(winner.getHoverName().getString()).trim();
         phase = LANDING;
         landStart = System.currentTimeMillis();
      }
   }

   public static void spin(List<ItemStack> pool, ItemStack winner) {
      begin(pool);
      land(winner);
   }

   private static float offset() {
      long now = System.currentTimeMillis();
      if (phase == FREE) {
         return (float)(now - freeStart) * FREE_SPEED;
      } else if (phase != LANDING) {
         return 0.0F;
      } else {
         long elapsed = now - landStart;
         if (elapsed >= LAND_MILLIS) {
            return landTo;
         } else {
            float t = (float)elapsed / (float)LAND_MILLIS;
            float left = 1.0F - t;
            float eased = 1.0F - left * left * left;
            return landFrom + (landTo - landFrom) * eased;
         }
      }
   }

   private static void render(GuiGraphicsExtractor graphics, DeltaTracker tracker) {
      Minecraft client = Minecraft.getInstance();
      if (phase != IDLE && !reel.isEmpty() && client.player != null) {
         long now = System.currentTimeMillis();
         if (phase == FREE && now - freeStart > FREE_LIMIT) {
            phase = IDLE;
            reel.clear();
         } else if (phase == LANDING && now - landStart > LAND_MILLIS + HOLD_MILLIS) {
            phase = IDLE;
            reel.clear();
         } else {
            int width = graphics.guiWidth();
            int centre = width / 2;
            int top = graphics.guiHeight() / 2 - ROW_HEIGHT;
            float scroll = offset();
            graphics.fill(0, top, width, top + ROW_HEIGHT, BACKDROP);
            graphics.fill(0, top, width, top + 1, EDGE);
            graphics.fill(0, top + ROW_HEIGHT - 1, width, top + ROW_HEIGHT, EDGE);
            graphics.pose().pushMatrix();
            graphics.pose().translate((float)centre, (float)(top + ROW_HEIGHT / 2));
            graphics.pose().scale(SCALE, SCALE);
            int first = Math.max(0, (int)((scroll - (float)width) / (float)PITCH));
            int last = Math.min(reel.size() - 1, (int)((scroll + (float)width) / (float)PITCH));

            for(int i = first; i <= last; ++i) {
               float x = (float)(i * PITCH) - scroll;
               graphics.item(reel.get(i), Math.round(x) - 8, -8);
            }

            graphics.pose().popMatrix();
            graphics.fill(centre - 1, top + 1, centre + 1, top + ROW_HEIGHT - 1, MARKER);
            if (phase == LANDING && now - landStart >= LAND_MILLIS && !prize.isEmpty()) {
               Font font = client.font;
               graphics.text(font, prize, centre - font.width(prize) / 2, top + ROW_HEIGHT + 4, LABEL, true);
            }

         }
      }
   }
}

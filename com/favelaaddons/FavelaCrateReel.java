package com.favelaaddons;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public class FavelaCrateReel {
   private static final int STRIP = 48;
   private static final int WINNER = 42;
   private static final int PITCH = 24;
   private static final long SPIN_MILLIS = 4600L;
   private static final long HOLD_MILLIS = 2600L;
   private static final float SCALE = 2.0F;
   private static final int ROW_HEIGHT = 34;
   private static final int BACKDROP = -1777726976;
   private static final int EDGE = -14671840;
   private static final int MARKER = -1146130;
   private static final int LABEL = -1;
   private static final List<ItemStack> reel = new ArrayList();
   private static long spinStart = 0L;
   private static String prize = "";

   public static void registrar() {
      HudElementRegistry.addLast(Identifier.parse("favelaaddons:crate_reel"), FavelaCrateReel::render);
   }

   public static boolean spinning() {
      return !reel.isEmpty() && System.currentTimeMillis() - spinStart < SPIN_MILLIS + HOLD_MILLIS;
   }

   public static void spin(List<ItemStack> pool, ItemStack winner) {
      reel.clear();
      if (winner != null && !winner.isEmpty()) {
         List<ItemStack> filler = new ArrayList();

         for(ItemStack stack : pool) {
            if (stack != null && !stack.isEmpty()) {
               filler.add(stack);
            }
         }

         if (filler.isEmpty()) {
            filler.add(winner);
         }

         java.util.Random shuffle = new java.util.Random();

         for(int i = 0; i < STRIP; ++i) {
            reel.add(i == WINNER ? winner : (ItemStack)filler.get(shuffle.nextInt(filler.size())));
         }

         prize = FavelaDisplays.sanitize(winner.getHoverName().getString());
         spinStart = System.currentTimeMillis();
      }
   }

   private static float eased(float t) {
      float left = 1.0F - t;
      return 1.0F - left * left * left;
   }

   private static void render(GuiGraphicsExtractor graphics, DeltaTracker tracker) {
      Minecraft client = Minecraft.getInstance();
      if (!reel.isEmpty() && client.player != null) {
         long elapsed = System.currentTimeMillis() - spinStart;
         if (elapsed > SPIN_MILLIS + HOLD_MILLIS) {
            reel.clear();
         } else {
            int width = graphics.guiWidth();
            int centre = width / 2;
            int top = graphics.guiHeight() / 2 - ROW_HEIGHT;
            float progress = elapsed >= SPIN_MILLIS ? 1.0F : (float)elapsed / (float)SPIN_MILLIS;
            float offset = eased(progress) * (float)(WINNER * PITCH);
            graphics.fill(0, top, width, top + ROW_HEIGHT, BACKDROP);
            graphics.fill(0, top, width, top + 1, EDGE);
            graphics.fill(0, top + ROW_HEIGHT - 1, width, top + ROW_HEIGHT, EDGE);
            graphics.pose().pushMatrix();
            graphics.pose().translate((float)centre, (float)(top + ROW_HEIGHT / 2));
            graphics.pose().scale(SCALE, SCALE);

            for(int i = 0; i < reel.size(); ++i) {
               float x = (float)(i * PITCH) - offset;
               if (!(x < -(float)width) && !(x > (float)width)) {
                  graphics.item((ItemStack)reel.get(i), Math.round(x) - 8, -8);
               }
            }

            graphics.pose().popMatrix();
            graphics.fill(centre - 1, top + 1, centre + 1, top + ROW_HEIGHT - 1, MARKER);
            if (elapsed >= SPIN_MILLIS && !prize.isEmpty()) {
               Font font = client.font;
               int y = top + ROW_HEIGHT + 4;
               graphics.text(font, prize, centre - font.width(prize) / 2, y, LABEL, true);
            }

         }
      }
   }
}

package com.favelaaddons;

import java.util.Locale;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public class FavelaPrimed {
   private static final int WAIT_COLOR = 0xFFE87878;
   private static final int READY_COLOR = 0xFF13A10E;
   private static final String READY_TEXT = "READY";
   private static long lastSwing = 0L;
   private static ItemStack cachedStack = null;
   private static boolean cachedPrimed = false;

   public static void registrar() {
      HudElementRegistry.addLast(Identifier.parse("favelaaddons:primed"), FavelaPrimed::render);
   }

   public static void onSwing(LocalPlayer player, InteractionHand hand) {
      if (Config.primedTimer && hand == InteractionHand.MAIN_HAND && player != null && isPrimed(player.getMainHandItem())) {
         lastSwing = System.currentTimeMillis();
      }

   }

   public static void reset() {
      lastSwing = 0L;
      cachedStack = null;
      cachedPrimed = false;
   }

   public static boolean isPrimed(ItemStack stack) {
      if (stack != null && !stack.isEmpty()) {
         if (stack == cachedStack) {
            return cachedPrimed;
         } else {
            boolean primed = FavelaTraits.hasTrait(stack, FavelaTraits.PRIMED);
            cachedStack = stack;
            cachedPrimed = primed;
            return primed;
         }
      } else {
         return false;
      }
   }

   public static long remaining() {
      if (lastSwing == 0L) {
         return 0L;
      } else {
         long left = (long)Math.max(1, Config.primedTimerMs) - (System.currentTimeMillis() - lastSwing);
         return left < 0L ? 0L : left;
      }
   }

   public static String label() {
      long left = remaining();
      return left <= 0L ? READY_TEXT : String.format(Locale.ROOT, "%.1f", (double)left / 1000.0);
   }

   private static void render(GuiGraphicsExtractor graphics, DeltaTracker tracker) {
      Minecraft client = Minecraft.getInstance();
      if (Config.primedTimer && !FavelaCrateReel.spinning() && client.player != null && isPrimed(client.player.getMainHandItem())) {
         Font font = client.font;
         String text = Config.primedTimerLabel + label();
         graphics.pose().pushMatrix();
         graphics.pose().translate((float)Config.primedTimerX, (float)Config.primedTimerY);
         graphics.pose().scale(Config.primedTimerScale, Config.primedTimerScale);
         graphics.text(font, text, 0, 0, remaining() <= 0L ? READY_COLOR : WAIT_COLOR, true);
         graphics.pose().popMatrix();
      }

   }
}

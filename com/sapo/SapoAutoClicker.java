package com.sapo;

import net.minecraft.ChatFormatting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public class SapoAutoClicker {
   public static final String PRIMED_TRAIT = "primed";
   private static long lastAllowedSwing = 0L;
   private static ItemStack cachedStack = null;
   private static boolean cachedPrimed = false;

   public static boolean shouldBlockSwing(LocalPlayer player, InteractionHand hand) {
      if (Build.FULL && Config.primedLimiter && hand == InteractionHand.MAIN_HAND && player != null) {
         if (!isPrimedWeapon(player.getMainHandItem())) {
            return false;
         } else if (!isAutoClickerSwing()) {
            return false;
         } else {
            long interval = (long)Math.max(1, Config.primedIntervalMs);
            long now = System.currentTimeMillis();
            long elapsed = now - lastAllowedSwing;
            if (elapsed < interval) {
               if (Config.devMode) {
                  System.out.println("[FA AutoClicker] Primed swing blocked, " + (interval - elapsed) + "ms remaining.");
               }

               return true;
            } else {
               lastAllowedSwing = now;
               if (Config.devMode) {
                  System.out.println("[FA AutoClicker] Primed swing allowed, next one in " + interval + "ms.");
               }

               return false;
            }
         }
      } else {
         return false;
      }
   }

   public static boolean isPrimedWeapon(ItemStack stack) {
      if (stack != null && !stack.isEmpty()) {
         if (stack == cachedStack) {
            return cachedPrimed;
         } else {
            boolean primed = false;

            for(String trait : SapoTraits.traits(stack)) {
               if (trait.toLowerCase().contains(PRIMED_TRAIT)) {
                  primed = true;
                  break;
               }
            }

            cachedStack = stack;
            cachedPrimed = primed;
            return primed;
         }
      } else {
         return false;
      }
   }

   public static String sanitize(String rawName) {
      String strippedName = ChatFormatting.stripFormatting(rawName);
      if (strippedName == null) {
         strippedName = rawName;
      }

      StringBuilder sb = new StringBuilder();

      for(int i = 0; i < strippedName.length(); ++i) {
         char c = strippedName.charAt(i);
         if (c != '[' && c != ']' && (c < '\ue000' || c > '\uf8ff')) {
            sb.append(c);
         }
      }

      return sb.toString().trim().toLowerCase();
   }

   private static boolean isAutoClickerSwing() {
      return StackWalker.getInstance().walk((frames) -> frames.limit(32L).anyMatch((frame) -> frame.getClassName().startsWith("me.melinoe.")));
   }
}

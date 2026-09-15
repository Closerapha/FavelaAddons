package com.favelaaddons;

import com.mojang.blaze3d.platform.InputConstants.Type;
import java.util.Random;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping.Category;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class FavelaCroak {
   private static long lastCroak = 0L;
   private static long currentInterval = 0L;
   private static final Random r = new Random();
   private static KeyMapping toggleKey;
   private static final KeyMapping.Category CATEGORY_FAVELA = Category.register(Identifier.parse("favelaaddons:croak"));

   public static void registrar() {
      toggleKey = KeyMappingHelper.registerKeyMapping(new KeyMapping("Toggle Croak", Type.KEYSYM, 86, CATEGORY_FAVELA));
      ClientTickEvents.END_CLIENT_TICK.register(FavelaCroak::checkLilypad);
   }

   private static void checkLilypad(Minecraft client) {
      if (toggleKey != null) {
         while(toggleKey.consumeClick()) {
            Config.active = !Config.active;
            Config.save();
            if (client.player != null) {
               client.player.sendSystemMessage(Component.literal("Croak " + (Config.active ? "§aEnabled" : "§cDisabled")));
            }
         }
      }

      if (Config.active && client.screen == null && client.player != null) {
         long windowHandle = client.getWindow().handle();
         boolean space = GLFW.glfwGetKey(windowHandle, 32) == 1;
         if (space && client.player.onGround()) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastCroak >= currentInterval) {
               client.player.jumpFromGround();
               int cps = Config.minCroaks;
               if (Config.maxCroaks > Config.minCroaks) {
                  cps += r.nextInt(Config.maxCroaks - Config.minCroaks + 1);
               } else if (cps <= 0) {
                  cps = 1;
               }

               currentInterval = 1000L / (long)cps;
               lastCroak = currentTime;
            }
         }

      }
   }
}

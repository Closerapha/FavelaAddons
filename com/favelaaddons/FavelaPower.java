package com.favelaaddons;

import com.mojang.blaze3d.platform.InputConstants.Type;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.KeyMapping.Category;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class FavelaPower {
   private static final KeyMapping.Category CATEGORY_POWER = Category.register(Identifier.parse("favelaaddons:power"));
   private static final String NAME = "FavelaAddons";
   private static KeyMapping killKey;
   private static boolean live = true;

   public static void registrar() {
      killKey = KeyMappingHelper.registerKeyMapping(new KeyMapping("Toggle " + NAME, Type.KEYSYM, 86, CATEGORY_POWER));
      ClientTickEvents.END_CLIENT_TICK.register(FavelaPower::listen);
   }

   public static boolean on() {
      return live;
   }

   public static boolean off() {
      return !live;
   }

   private static void listen(Minecraft client) {
      if (killKey != null) {
         while(killKey.consumeClick()) {
            live = !live;
            if (client.player != null) {
               client.player.sendSystemMessage(Component.literal(NAME + " " + (live ? "§aEnabled" : "§cDisabled")));
            }
         }
      }
   }
}

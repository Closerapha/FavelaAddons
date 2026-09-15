package com.sapo;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;

public class SapoArmorSwap {
   private static final int STEP_SELECT = 0;
   private static final int STEP_USE = 1;
   private static final List<int[]> queue = new ArrayList();
   private static int cooldown = 0;
   private static int stepDelay = 2;
   private static int previousSlot = -1;

   public static void registrar() {
      ClientTickEvents.END_CLIENT_TICK.register(SapoArmorSwap::onTick);
   }

   public static Config.ArmorSetup findSetupForKey(int key) {
      if (Config.armorSwapEnabled && key > 0) {
         for(Config.ArmorSetup setup : Config.armorSetups) {
            if (setup != null && setup.enabled && setup.key == key) {
               return setup;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   public static void trigger(Minecraft client, Config.ArmorSetup setup) {
      if (setup != null && client.player != null && client.screen == null) {
         if (queue.isEmpty()) {
            previousSlot = client.player.getInventory().getSelectedSlot();
            stepDelay = Math.max(1, setup.cooldownTicks);
            int queued = 0;

            for(int slot : setup.slots()) {
               if (slot >= 1 && slot <= 9) {
                  queue.add(new int[]{STEP_SELECT, slot - 1});
                  queue.add(new int[]{STEP_USE, slot - 1});
                  ++queued;
               }
            }

            if (queued == 0) {
               client.player.sendSystemMessage(Component.literal("§c[FavelaClient] " + setup.name + " has no slots configured."));
            } else {
               if (Config.armorSwapReturn && previousSlot >= 0) {
                  queue.add(new int[]{STEP_SELECT, previousSlot});
               }

               cooldown = 0;
               if (Config.devMode) {
                  System.out.println("[FA ArmorSwap] " + setup.name + ": queued " + queued + " slot(s), " + stepDelay + " tick(s) apart.");
               }

            }
         }
      }
   }

   private static void onTick(Minecraft client) {
      if (!queue.isEmpty()) {
         if (client.player == null || client.level == null) {
            queue.clear();
         } else if (cooldown > 0) {
            --cooldown;
         } else {
            int[] step = (int[])queue.remove(0);
            Inventory inventory = client.player.getInventory();

            try {
               if (step[0] == STEP_SELECT) {
                  inventory.setSelectedSlot(step[1]);
                  if (Config.devMode) {
                     System.out.println("[FA ArmorSwap] Selected hotbar slot " + (step[1] + 1) + ".");
                  }
               } else if (client.gameMode != null) {
                  client.gameMode.useItem(client.player, InteractionHand.MAIN_HAND);
                  client.player.swing(InteractionHand.MAIN_HAND);
                  if (Config.devMode) {
                     System.out.println("[FA ArmorSwap] Used item on slot " + (step[1] + 1) + ".");
                  }
               }
            } catch (Exception e) {
               queue.clear();
               System.out.println("[FA ArmorSwap] Error during swap: " + e.getMessage());
            }

            cooldown = stepDelay;
         }

      }
   }
}

package com.favelaaddons.client.mixin;

import com.favelaaddons.Config;
import com.favelaaddons.FavelaArmorSwap;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({KeyboardHandler.class})
public class FavelaArmorSwapMixin {
   @Inject(
      method = {"keyPress"},
      at = {@At("HEAD")}
   )
   private void favelaArmorSwap(long window, int action, KeyEvent keyEvent, CallbackInfo ci) {
      if (action == 1 && Config.armorSwapEnabled) {
         Config.ArmorSetup setup = FavelaArmorSwap.findSetupForKey(keyEvent.key());
         if (setup != null) {
            Minecraft client = Minecraft.getInstance();
            if (client.screen == null && window == client.getWindow().handle()) {
               FavelaArmorSwap.trigger(client, setup);
            }
         }
      }
   }
}

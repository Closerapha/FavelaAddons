package com.favelaaddons.client.mixin;

import com.favelaaddons.Config;
import com.favelaaddons.FavelaDebugger;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({KeyboardHandler.class})
public class FavelaDebuggerMixin {
   @Inject(
      method = {"keyPress"},
      at = {@At("HEAD")}
   )
   private void favelaDebugScan(long window, int action, KeyEvent keyEvent, CallbackInfo ci) {
      if (action == 1 && Config.debuggerEnabled && Config.debuggerKey > 0 && keyEvent.key() == Config.debuggerKey) {
         Minecraft client = Minecraft.getInstance();
         if (client.screen == null && window == client.getWindow().handle()) {
            FavelaDebugger.dumpEntities(client);
         }
      }
   }
}

package com.favelaaddons.client.mixin;

import com.favelaaddons.FavelaAutoClicker;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({LocalPlayer.class})
public class FavelaAutoClickerMixin {
   @Inject(
      method = {"swing(Lnet/minecraft/world/InteractionHand;)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void favela$limitPrimedSwing(InteractionHand hand, CallbackInfo ci) {
      try {
         if (FavelaAutoClicker.shouldBlockSwing((LocalPlayer)(Object)this, hand)) {
            ci.cancel();
         }
      } catch (Exception e) {
         System.out.println("[FA Debug] Error limiting Auto Clicker swing: " + e.getMessage());
      }

   }
}

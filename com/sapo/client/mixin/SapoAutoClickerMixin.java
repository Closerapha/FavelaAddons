package com.sapo.client.mixin;

import com.sapo.SapoAutoClicker;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({LocalPlayer.class})
public class SapoAutoClickerMixin {
   @Inject(
      method = {"swing(Lnet/minecraft/world/InteractionHand;)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void sapo$limitPrimedSwing(InteractionHand hand, CallbackInfo ci) {
      try {
         if (SapoAutoClicker.shouldBlockSwing((LocalPlayer)(Object)this, hand)) {
            ci.cancel();
         }
      } catch (Exception e) {
         System.out.println("[FA Debug] Error limiting Auto Clicker swing: " + e.getMessage());
      }

   }
}

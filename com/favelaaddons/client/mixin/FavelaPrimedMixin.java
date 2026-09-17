package com.favelaaddons.client.mixin;

import com.favelaaddons.FavelaPrimed;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({LocalPlayer.class})
public class FavelaPrimedMixin {
   @Inject(
      method = {"swing(Lnet/minecraft/world/InteractionHand;)V"},
      at = {@At("HEAD")},
      require = 0
   )
   private void favela$trackPrimedSwing(InteractionHand hand, CallbackInfo ci) {
      try {
         FavelaPrimed.onSwing((LocalPlayer)(Object)this, hand);
      } catch (Exception var3) {
      }

   }
}

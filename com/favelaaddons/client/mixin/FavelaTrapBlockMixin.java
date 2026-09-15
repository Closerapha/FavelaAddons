package com.favelaaddons.client.mixin;

import com.favelaaddons.FavelaTrapCounter;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({MultiPlayerGameMode.class})
public class FavelaTrapBlockMixin {
   @Inject(
      method = {"useItem"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void favela$blockTrapUse(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
      if (FavelaTrapCounter.shouldBlockUse(player, hand)) {
         cir.setReturnValue(InteractionResult.FAIL);
      }

   }

   @Inject(
      method = {"useItemOn"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void favela$blockTrapUseOn(LocalPlayer player, InteractionHand hand, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
      if (FavelaTrapCounter.shouldBlockUse(player, hand)) {
         cir.setReturnValue(InteractionResult.FAIL);
      }

   }

   @Inject(
      method = {"interact"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void favela$blockTrapInteract(Player player, Entity target, EntityHitResult hit, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
      if (FavelaTrapCounter.shouldBlockUse(player, hand)) {
         cir.setReturnValue(InteractionResult.FAIL);
      }

   }
}

package com.favelaaddons.client.mixin;

import com.favelaaddons.Config;
import com.favelaaddons.FavelaMod;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({SoundEngine.class})
public class FavelaAudioMixin {
   @Inject(
      method = {"play"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void play(SoundInstance sound, CallbackInfoReturnable<Void> cir) {
      if (sound != null) {
         try {
            if (Config.aliveOrDeadMode) {
               String soundId = sound.getIdentifier().toString();
               if (soundId.contains("noise:cherub.beam1")) {
                  FavelaMod.aliveOrDeadMessage = "CROUCH!";
                  FavelaMod.alertTimeRemaining = 20;
                  FavelaMod.aliveOrDeadColor = 5635925;
               } else if (soundId.contains("noise:cherub.beam2")) {
                  FavelaMod.aliveOrDeadMessage = "JUMP!";
                  FavelaMod.alertTimeRemaining = 20;
                  FavelaMod.aliveOrDeadColor = 5592575;
               }
            }
         } catch (Exception var1) {
         }

      }
   }
}

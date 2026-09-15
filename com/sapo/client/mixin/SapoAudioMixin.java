package com.sapo.client.mixin;

import com.sapo.Config;
import com.sapo.SapoMod;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({SoundEngine.class})
public class SapoAudioMixin {
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
                  SapoMod.aliveOrDeadMessage = "CROUCH!";
                  SapoMod.alertTimeRemaining = 20;
                  SapoMod.aliveOrDeadColor = 5635925;
               } else if (soundId.contains("noise:cherub.beam2")) {
                  SapoMod.aliveOrDeadMessage = "JUMP!";
                  SapoMod.alertTimeRemaining = 20;
                  SapoMod.aliveOrDeadColor = 5592575;
               }
            }
         } catch (Exception e) {
            System.out.println("[FA Debug] Error getting sound: " + e.getMessage());
            e.printStackTrace();
         }

      }
   }
}

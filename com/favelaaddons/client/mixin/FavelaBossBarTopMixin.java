package com.favelaaddons.client.mixin;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.favelaaddons.FavelaBossBar;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({BossHealthOverlay.class})
public class FavelaBossBarTopMixin {
   @Inject(
      method = {"extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V"},
      at = {@At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)V",
   shift = At.Shift.AFTER
)},
      require = 0
   )
   private void favela$drawBarOnTop(GuiGraphicsExtractor graphics, CallbackInfo ci) {
      try {
         if (FavelaBossBar.onTop()) {
            for(int i = 0; i < FavelaBossBar.recorded(); ++i) {
               int[] at = FavelaBossBar.geometryAt(i);
               RenderPipeline pipeline = (RenderPipeline)FavelaBossBar.pipelineAt(i);
               Identifier sprite = (Identifier)FavelaBossBar.spriteAt(i);
               if (at[8] == 0) {
                  graphics.blitSprite(pipeline, sprite, at[0], at[1], at[2], at[3], at[4], at[5], at[6], at[7]);
               } else {
                  graphics.blitSprite(pipeline, sprite, at[0], at[1], at[2], at[3], at[4], at[5], at[6], at[7], at[8]);
               }
            }

            FavelaBossBar.forgetBlits();
         }
      } catch (Exception var7) {
      }

   }
}

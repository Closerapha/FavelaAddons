package com.favelaaddons.client.mixin;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.favelaaddons.FavelaBossBar;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.resources.Identifier;
import net.minecraft.world.BossEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin({BossHealthOverlay.class})
public class FavelaBossBarBarMixin {
   @Redirect(
      method = {"extractBar(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IILnet/minecraft/world/BossEvent;I[Lnet/minecraft/resources/Identifier;[Lnet/minecraft/resources/Identifier;)V"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/world/BossEvent;getColor()Lnet/minecraft/world/BossEvent$BossBarColor;"
),
      require = 0
   )
   private BossEvent.BossBarColor favela$neutralSprite(BossEvent event) {
      try {
         return FavelaBossBar.spriteColour(event.getColor());
      } catch (Exception var3) {
         return event.getColor();
      }
   }

   @Redirect(
      method = {"extractBar(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IILnet/minecraft/world/BossEvent;I[Lnet/minecraft/resources/Identifier;[Lnet/minecraft/resources/Identifier;)V"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIIIIIII)V"
),
      require = 0
   )
   private void favela$tintBar(GuiGraphicsExtractor graphics, RenderPipeline pipeline, Identifier sprite, int textureWidth, int textureHeight, int u, int v, int x, int y, int width, int height) {
      int tint = 0;

      try {
         tint = FavelaBossBar.barTint();
      } catch (Exception var13) {
      }

      if (tint == 0) {
         graphics.blitSprite(pipeline, sprite, textureWidth, textureHeight, u, v, x, y, width, height);
      } else {
         graphics.blitSprite(pipeline, sprite, textureWidth, textureHeight, u, v, x, y, width, height, tint);
      }

   }
}

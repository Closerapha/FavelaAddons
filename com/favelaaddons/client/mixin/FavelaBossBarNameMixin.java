package com.favelaaddons.client.mixin;

import com.favelaaddons.FavelaBossBar;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin({BossHealthOverlay.class})
public class FavelaBossBarNameMixin {
   @ModifyVariable(
      method = {"extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V"},
      at = @At("STORE"),
      ordinal = 0,
      require = 0
   )
   private Component favela$tintBossBarName(Component name) {
      try {
         return FavelaBossBar.tint(name);
      } catch (Exception var3) {
         return name;
      }
   }
}

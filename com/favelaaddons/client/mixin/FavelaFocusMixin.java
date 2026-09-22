package com.favelaaddons.client.mixin;

import com.favelaaddons.FavelaPower;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({AbstractContainerEventHandler.class})
public class FavelaFocusMixin {
   @Inject(
      method = {"setFocused(Lnet/minecraft/client/gui/components/events/GuiEventListener;)V"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 0
   )
   private void favela$keepFocus(GuiEventListener listener, CallbackInfo ci) {
      if (FavelaPower.off()) {
         return;
      }

      if (listener != null) {
         AbstractContainerEventHandler self = (AbstractContainerEventHandler)(Object)this;
         if (listener == self.getFocused()) {
            ci.cancel();
         }
      }
   }
}

package com.favelaaddons;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.resources.Identifier;
import com.favelaaddons.client.mixin.FavelaBossBarAccessor;

public class FavelaCalls {
   private static UUID watching = null;
   private static float progress = 1.0F;
   private static boolean ambushCalled = false;
   private static boolean deathmarkCalled = false;
   private static String message = "";
   private static int color = 16755200;
   private static int ticksLeft = 0;

   public static void registrar() {
      ClientTickEvents.END_CLIENT_TICK.register(FavelaCalls::onTick);
      HudElementRegistry.addLast(Identifier.parse("favelaaddons:calls"), FavelaCalls::render);
   }

   public static Collection<LerpingBossEvent> bossBars() {
      try {
         Gui gui = Minecraft.getInstance().gui;
         return (Collection<LerpingBossEvent>)(gui == null ? List.of() : ((FavelaBossBarAccessor)gui.getBossOverlay()).favela$events().values());
      } catch (Throwable e) {

         return List.of();
      }
   }

   private static void onTick(Minecraft client) {
      if (FavelaPower.off()) {
         return;
      }

      if (ticksLeft > 0) {
         --ticksLeft;
      }

      if (Config.calls && client.player != null) {
         LerpingBossEvent bar = FavelaBossHp.activeBar();

         if (bar == null) {
            forget();
         } else {
            float now = bar.getProgress();
            if (!bar.getId().equals(watching)) {
               watching = bar.getId();
               ambushCalled = false;
               deathmarkCalled = false;
            } else if (now > progress + 0.05F) {
               ambushCalled = false;
               deathmarkCalled = false;
            }

            progress = now;
            announce();
         }
      } else {
         forget();
      }
   }

   private static void announce() {
      float ambushAt = (float)Math.clamp((long)Config.ambushAt, 1L, 100L) / 100.0F;
      float deathmarkAt = (float)Math.clamp((long)Config.deathmarkAt, 1L, 100L) / 100.0F;
      if (!deathmarkCalled && progress <= deathmarkAt) {
         deathmarkCalled = true;
         ambushCalled = true;
         show(Config.deathmarkText, Config.deathmarkColor);
      } else if (!ambushCalled && progress <= ambushAt) {
         ambushCalled = true;
         show(Config.ambushText, Config.ambushColor);
      }

   }

   private static void show(String wording, int wordingColor) {
      if (wording != null && !wording.trim().isEmpty()) {
         message = wording.trim();
         color = wordingColor;
         ticksLeft = Math.max(1, Config.callStayTicks);
         if (Config.callSound) {
            FavelaMod.playExternalSound();
         }


      }
   }

   public static void preview() {
      show(Config.ambushText, Config.ambushColor);
   }

   private static void forget() {
      watching = null;
      progress = 1.0F;
      ambushCalled = false;
      deathmarkCalled = false;
   }

   private static void render(GuiGraphicsExtractor graphics, DeltaTracker tracker) {
      if (FavelaPower.on() && !FavelaCrateReel.spinning()) {
         Minecraft client = Minecraft.getInstance();
         if (Config.calls && ticksLeft > 0 && !message.isEmpty() && client.player != null) {
            Font font = client.font;
            graphics.pose().pushMatrix();
            graphics.pose().translate((float)Config.callX, (float)Config.callY);
            graphics.pose().scale(Config.callScale, Config.callScale);
            graphics.text(font, message, 0, 0, color | -16777216, true);
            graphics.pose().popMatrix();
         }

            }

   }
}

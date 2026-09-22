package com.favelaaddons;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.resources.Identifier;

public class FavelaBossHp {
   private static final int HIGH = -11141291;
   private static final int MID = -171;
   private static final int LOW = -43691;

   private static final int TELOS_BAR_COUNT = 5;
   private static final int DEATH_TICKS = 60;
   private static final float ZERO = 1.0E-4F;
   private static final float DEATH_EDGE = 0.02F;
   private static float lastProgress = -1.0F;
   private static int deathTicks = 0;

   public static void registrar() {
      ClientTickEvents.END_CLIENT_TICK.register(FavelaBossHp::onTick);
      HudElementRegistry.addLast(Identifier.parse("favelaaddons:boss_hp"), FavelaBossHp::render);
   }

   private static void onTick(Minecraft client) {
      if (FavelaPower.off()) {
         return;
      }

      float now = progress();
      if (now >= 0.0F) {
         if (now <= ZERO && lastProgress > ZERO) {
            deathTicks = DEATH_TICKS;
         }

         lastProgress = now;
      } else {
         if (lastProgress >= 0.0F && lastProgress <= DEATH_EDGE) {
            deathTicks = DEATH_TICKS;
         }

         lastProgress = -1.0F;
      }

      if (deathTicks > 0) {
         --deathTicks;
      }

   }

   public static LerpingBossEvent activeBar() {
      List<LerpingBossEvent> bars = new ArrayList(FavelaCalls.bossBars());
      if (bars.isEmpty()) {
         return null;
      } else {
         if (bars.size() == TELOS_BAR_COUNT) {
            LerpingBossEvent tracked = (LerpingBossEvent)bars.get(1);
            if (tracked != null && tracked.getProgress() > 0.0F) {
               return tracked;
            }
         }

         for(LerpingBossEvent bar : bars) {
            float progress = bar.getProgress();
            if (progress > 0.0F && progress <= 1.0F) {
               return bar;
            }
         }

         return null;
      }
   }

   public static float progress() {
      LerpingBossEvent bar = activeBar();
      return bar == null ? -1.0F : bar.getProgress();
   }

   public static String format(float progress) {
      String percent = Config.bossHpDecimals > 0 ? String.format(Locale.ROOT, "%." + Math.min(3, Config.bossHpDecimals) + "f", progress * 100.0F) : String.valueOf(Math.round(progress * 100.0F));
      return Config.bossHpLabel + percent + "%";
   }

   public static int colorFor(float progress) {
      if (!Config.bossHpColorByHealth) {
         return Config.bossHpColor;
      } else {
         return progress <= 0.25F ? LOW : (progress <= 0.6F ? MID : HIGH);
      }
   }

   private static void render(GuiGraphicsExtractor graphics, DeltaTracker tracker) {
      if (FavelaPower.on() && !FavelaCrateReel.spinning()) {
         Minecraft client = Minecraft.getInstance();
         if (Config.bossHp && client.player != null) {
            float progress = progress();
            boolean dying = progress <= ZERO && deathTicks > 0;
            if (progress > ZERO || dying) {
               if (dying) {
                  progress = 0.0F;
               }

               Font font = client.font;
               graphics.pose().pushMatrix();
               graphics.pose().translate((float)Config.bossHpX, (float)Config.bossHpY);
               graphics.pose().scale(Config.bossHpScale, Config.bossHpScale);
               graphics.text(font, format(progress), 0, 0, colorFor(progress) | -16777216, true);
               graphics.pose().popMatrix();
            }

         }
            }

   }
}

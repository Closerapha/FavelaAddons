package com.favelaaddons;

import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.world.BossEvent;
import net.minecraft.network.chat.Style;

public class FavelaBossBar {
   private static boolean hooked = false;

   public static boolean hooked() {
      return hooked;
   }

   private static boolean paintCurrent = false;
   private static final int MAX_BLITS = 4;
   private static final Object[] SPRITES = new Object[MAX_BLITS];
   private static final Object[] PIPELINES = new Object[MAX_BLITS];
   private static final int[][] GEOMETRY = new int[MAX_BLITS][9];
   private static int blits = 0;

   public static void forgetBlits() {
      blits = 0;
   }

   public static void record(Object pipeline, Object sprite, int textureWidth, int textureHeight, int u, int v, int x, int y, int width, int height, int tint) {
      if (blits < MAX_BLITS) {
         PIPELINES[blits] = pipeline;
         SPRITES[blits] = sprite;
         GEOMETRY[blits] = new int[]{textureWidth, textureHeight, u, v, x, y, width, height, tint};
         ++blits;
      }

   }

   public static int recorded() {
      return blits;
   }

   public static Object pipelineAt(int i) {
      return PIPELINES[i];
   }

   public static Object spriteAt(int i) {
      return SPRITES[i];
   }

   public static int[] geometryAt(int i) {
      return GEOMETRY[i];
   }

   public static boolean onTop() {
      return Config.bossBarOnTop;
   }

   private static boolean wanted() {
      if (!Config.vulnHud) {
         return false;
      } else {
         int state = FavelaVuln.state();
         return state != FavelaVuln.VULNERABLE;
      }
   }

   private static int stateColour() {
      int colour = FavelaVuln.state() == FavelaVuln.RESISTANT ? Config.vulnResistantColor : Config.vulnInvulnerableColor;
      return colour | -16777216;
   }

   public static int barTint() {
      return paintCurrent ? stateColour() : 0;
   }

   public static BossEvent.BossBarColor spriteColour(BossEvent event, BossEvent.BossBarColor original) {
      hooked = true;
      paintCurrent = wanted() && isTracked(event);
      return paintCurrent ? BossEvent.BossBarColor.WHITE : original;
   }

   private static boolean isTracked(BossEvent event) {
      try {
         LerpingBossEvent tracked = FavelaBossHp.activeBar();
         return tracked != null && event != null && tracked.getId().equals(event.getId());
      } catch (Exception var2) {
         return false;
      }
   }

   public static String describe(Component name) {
      if (name == null) {
         return "(no bar)";
      } else {
         StringBuilder out = new StringBuilder();
         int[] run = new int[]{0};
         name.visit((style, text) -> {
            if (!text.isEmpty()) {
               ++run[0];
               String font = FavelaFonts.fontName(style);
               String texture = "";

               for(int glyph : text.codePoints().toArray()) {
                  String found = FavelaFonts.texture(font, glyph);
                  if (found != null) {
                     texture = found;
                     break;
                  }
               }

               out.append("\n  ").append(run[0]).append(". font=").append(font);
               out.append(" colour=").append(style.getColor() == null ? "none" : String.format("#%06X", style.getColor().getValue()));
               out.append(" chars=").append(text.codePointCount(0, text.length()));
               out.append(" texture=").append(texture.isEmpty() ? "(unmapped)" : texture);
               StringBuilder readable = new StringBuilder();

               for(int glyph : text.codePoints().toArray()) {
                  if (glyph >= 32 && glyph < 127) {
                     readable.append((char)glyph);
                  }
               }

               String plain = readable.toString();
               if (!plain.trim().isEmpty()) {
                  out.append(" text=\"").append(plain.trim()).append("\"");
               }
            }

            return Optional.empty();
         }, Style.EMPTY);
         return out.length() == 0 ? "(empty)" : out.toString();
      }
   }
}

package com.favelaaddons;

import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.world.BossEvent;
import net.minecraft.network.chat.Style;

public class FavelaBossBar {
   private static boolean hooked = false;

   public static boolean hooked() {
      return hooked;
   }

   public static int barTint() {
      hooked = true;
      if (!Config.vulnHud) {
         return 0;
      } else {
         int state = FavelaVuln.state();
         if (state == FavelaVuln.VULNERABLE) {
            return 0;
         } else {
            int colour = state == FavelaVuln.RESISTANT ? Config.vulnResistantColor : Config.vulnInvulnerableColor;
            return colour | -16777216;
         }
      }
   }

   public static BossEvent.BossBarColor spriteColour(BossEvent.BossBarColor original) {
      return barTint() == 0 ? original : BossEvent.BossBarColor.WHITE;
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

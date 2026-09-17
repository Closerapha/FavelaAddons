package com.favelaaddons;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

public class FavelaBossBar {
   private static final Map<Component, Component> CACHE = new IdentityHashMap();
   private static final int MAX_CACHE = 16;
   private static int lastColour = 0;
   private static boolean hooked = false;

   public static boolean hooked() {
      return hooked;
   }

   public static Component tint(Component name) {
      hooked = true;
      if (name == null || !Config.vulnHud) {
         return name;
      } else {
         int state = FavelaVuln.state();
         if (state == FavelaVuln.VULNERABLE) {
            return name;
         } else {
            int colour = state == FavelaVuln.RESISTANT ? Config.vulnResistantColor : Config.vulnInvulnerableColor;
            if (colour != lastColour) {
               lastColour = colour;
               CACHE.clear();
            }

            Component cached = (Component)CACHE.get(name);
            if (cached != null) {
               return cached;
            } else {
               Component out = repaint(name, colour);
               if (CACHE.size() >= MAX_CACHE) {
                  CACHE.clear();
               }

               CACHE.put(name, out);
               return out;
            }
         }
      }
   }

   private static Component repaint(Component name, int colour) {
      MutableComponent out = Component.empty();
      TextColor wanted = TextColor.fromRgb(colour);
      name.visit((style, text) -> {
         if (!text.isEmpty()) {
            out.append(Component.literal(text).withStyle(style.withColor(wanted)));
         }

         return Optional.empty();
      }, Style.EMPTY);
      return out;
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

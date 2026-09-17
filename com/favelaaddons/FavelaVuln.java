package com.favelaaddons;

import java.util.Optional;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;

public class FavelaVuln {
   public static final int VULNERABLE = 0;
   public static final int RESISTANT = 1;
   public static final int INVULNERABLE = 2;
   private static final int NO_COLOUR = Integer.MIN_VALUE;
   private static final int MAX_DISPLAYS = 256;
   private static final int BLUE_MARGIN = 32;
   private static final int RED_MARGIN = 12;
   private static final String FILL_TEXTURE = "xikage/default/inner";
   private static int state = VULNERABLE;
   private static int lastFill = NO_COLOUR;
   private static int ticksLeft = 0;

   public static void registrar() {
      ClientTickEvents.END_CLIENT_TICK.register(FavelaVuln::onTick);
      HudElementRegistry.addLast(Identifier.parse("favelaaddons:vuln"), FavelaVuln::render);
   }

   public static int state() {
      return state;
   }

   public static int lastFill() {
      return lastFill;
   }

   private static void onTick(Minecraft client) {
      if (ticksLeft > 0) {
         --ticksLeft;
      } else if (Config.vulnHud && client.player != null && client.level != null && bossBarUp()) {
         ticksLeft = Math.max(1, Config.vulnCheckTicks) - 1;
         int fill = nearestFill(client.player);
         lastFill = fill;
         state = fill == NO_COLOUR ? VULNERABLE : classify(fill);
      } else {
         state = VULNERABLE;
         lastFill = NO_COLOUR;
      }
   }

   public static boolean bossBarUp() {
      try {
         for(net.minecraft.client.gui.components.LerpingBossEvent bar : FavelaCalls.bossBars()) {
            if (bar != null) {
               return true;
            }
         }
      } catch (Exception var2) {
      }

      return false;
   }

   public static int nearestFill(LocalPlayer player) {
      double range = (double)Math.max(8, Config.vulnRange);
      double rangeSqr = range * range;
      double bestDistance = Double.MAX_VALUE;
      int bestColour = NO_COLOUR;
      int seen = 0;

      try {
         Minecraft client = Minecraft.getInstance();
         if (client.level == null) {
            return NO_COLOUR;
         }

         for(Entity entity : client.level.entitiesForRendering()) {
            if (seen >= MAX_DISPLAYS) {
               break;
            }

            if (entity instanceof Display.TextDisplay) {
               Display.TextDisplay display = (Display.TextDisplay)entity;
               double distance = display.distanceToSqr(player);
               if (distance <= rangeSqr) {
                  ++seen;
                  int colour = fillColourOf(display);
                  if (colour != NO_COLOUR && distance < bestDistance) {
                     bestDistance = distance;
                     bestColour = colour;
                  }
               }
            }
         }
      } catch (Exception var14) {
      }

      return bestColour;
   }

   public static int fillColourOf(Display.TextDisplay display) {
      Component text = display.getText();
      if (text == null) {
         return NO_COLOUR;
      } else {
         {
            String wanted = FILL_TEXTURE;
            int[] found = new int[]{NO_COLOUR};
            text.visit((style, content) -> {
               String font = FavelaFonts.fontName(style);
               boolean draws = content.codePoints().anyMatch((glyph) -> FavelaFonts.textureMatches(font, glyph, wanted));
               if (!draws) {
                  return Optional.empty();
               } else {
                  found[0] = style.getColor() == null ? -1 : style.getColor().getValue();
                  return Optional.of(Boolean.TRUE);
               }
            }, Style.EMPTY);
            return found[0];
         }
      }
   }

   public static int classify(int colour) {
      if (colour == -1) {
         return VULNERABLE;
      } else {
         int red = colour >> 16 & 255;
         int green = colour >> 8 & 255;
         int blue = colour & 255;
         if (blue <= green + BLUE_MARGIN) {
            return VULNERABLE;
         } else {
            return red > green + RED_MARGIN ? RESISTANT : INVULNERABLE;
         }
      }
   }

   public static String describeBars() {
      Minecraft client = Minecraft.getInstance();
      if (client.player == null || client.level == null) {
         return "(no level)";
      } else {
         double range = (double)Math.max(8, Config.vulnRange);
         double rangeSqr = range * range;
         int displays = 0;
         int withFill = 0;
         StringBuilder colours = new StringBuilder();

         for(Entity entity : client.level.entitiesForRendering()) {
            if (entity instanceof Display.TextDisplay) {
               Display.TextDisplay display = (Display.TextDisplay)entity;
               if (display.distanceToSqr(client.player) <= rangeSqr) {
                  ++displays;
                  int colour = fillColourOf(display);
                  if (colour != NO_COLOUR) {
                     ++withFill;
                     if (colours.length() < 90) {
                        colours.append(colours.length() > 0 ? " " : "").append(String.format("#%06X", colour)).append("=").append(stateName(classify(colour)));
                     }
                  }
               }
            }
         }

         return displays + " text display(s), " + withFill + " with the bar texture" + (colours.length() > 0 ? " [" + colours + "]" : "");
      }
   }

   public static String stateName(int value) {
      switch (value) {
         case RESISTANT:
            return "resistant";
         case INVULNERABLE:
            return "invulnerable";
         default:
            return "hittable";
      }
   }

   private static String wording() {
      switch (state) {
         case RESISTANT:
            return Config.vulnResistantText;
         case INVULNERABLE:
            return Config.vulnInvulnerableText;
         default:
            return "";
      }
   }

   private static int colour() {
      return state == RESISTANT ? Config.vulnResistantColor : Config.vulnInvulnerableColor;
   }

   private static void render(GuiGraphicsExtractor graphics, DeltaTracker tracker) {
      Minecraft client = Minecraft.getInstance();
      if (Config.vulnHud && state != VULNERABLE && client.player != null) {
         String text = wording();
         if (text != null && !text.trim().isEmpty()) {
            Font font = client.font;
            graphics.pose().pushMatrix();
            graphics.pose().translate((float)Config.vulnX, (float)Config.vulnY);
            graphics.pose().scale(Config.vulnScale, Config.vulnScale);
            graphics.text(font, text.trim(), 0, 0, colour() | -16777216, true);
            graphics.pose().popMatrix();
         }
      }

   }
}

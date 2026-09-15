package com.favelaaddons;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;

public class FavelaTraits {
   private static final int STAT_COLOR = -5592406;
   private static final int TRAIT_COLOR = -11141291;
   private static final int MAX_TRAIT_WORDS = 3;
   private static final int MAX_TRAIT_LENGTH = 24;
   private static final int MIN_DESCRIPTION_LENGTH = 30;

   public static void registrar() {
      HudElementRegistry.addLast(Identifier.parse("favelaaddons:traits"), FavelaTraits::render);
   }

   public static List<String> loreLines(ItemStack stack) {
      List<String> lines = new ArrayList();
      if (stack != null && !stack.isEmpty()) {
         ItemLore lore = (ItemLore)stack.get(DataComponents.LORE);
         if (lore != null) {
            for(Component line : lore.lines()) {
               lines.add(line.getString());
            }
         }

      }
      return lines;
   }

   public static List<String> cleanLines(ItemStack stack) {
      List<String> clean = new ArrayList();

      for(String line : loreLines(stack)) {
         String text = FavelaAutoClicker.sanitize(line);
         if (!text.isEmpty()) {
            clean.add(text);
         }
      }

      return clean;
   }

   public static List<String> stats(ItemStack stack) {
      List<String> stats = new ArrayList();

      for(String line : cleanLines(stack)) {
         int marker = line.indexOf(187);
         if (marker > 0) {
            String key = line.substring(0, marker).trim();
            String value = line.substring(marker + 1).trim();
            if (!key.isEmpty() && !value.isEmpty()) {
               stats.add(key + " " + value);
            }
         }
      }

      return stats;
   }

   public static List<String> traits(ItemStack stack) {
      List<String> clean = cleanLines(stack);
      List<String> traits = new ArrayList();

      for(int i = 0; i < clean.size(); ++i) {
         String line = (String)clean.get(i);
         if (line.indexOf(187) < 0 && line.length() <= MAX_TRAIT_LENGTH && line.split("\s+").length <= MAX_TRAIT_WORDS && line.matches(".*[a-z].*")) {
            String next = i + 1 < clean.size() ? (String)clean.get(i + 1) : "";
            if (next.length() >= MIN_DESCRIPTION_LENGTH) {
               traits.add(line);
            }
         }
      }

      return traits;
   }

   private static void render(GuiGraphicsExtractor graphics, DeltaTracker tracker) {
      Minecraft client = Minecraft.getInstance();
      if (Config.traitDetector && Config.traitHud && client.player != null) {
         ItemStack held = client.player.getMainHandItem();
         List<String> traits = traits(held);
         List<String> stats = stats(held);
         if (!traits.isEmpty() || !stats.isEmpty()) {
            Font font = client.font;
            graphics.pose().pushMatrix();
            graphics.pose().translate((float)Config.traitHudX, (float)Config.traitHudY);
            graphics.pose().scale(Config.traitHudScale, Config.traitHudScale);
            int y = 0;
            if (!traits.isEmpty()) {
               graphics.text(font, String.join(" · ", traits), 0, y, TRAIT_COLOR, true);
               y += 10;
            }

            if (!stats.isEmpty()) {
               graphics.text(font, String.join("  ", stats), 0, y, STAT_COLOR, true);
            }

            graphics.pose().popMatrix();
         }

      }
   }
}

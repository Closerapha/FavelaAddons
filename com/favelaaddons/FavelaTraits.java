package com.favelaaddons;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;

public class FavelaTraits {
   private static final String[] TRAIT_NAMES = new String[]{"Airborne", "Dodgebreaker", "Burning Spirit", "Overdrive", "Discipline", "Spiritual Bond", "Ember Idol", "Aegis Idol", "Ambush", "Deathmark", "Resolve", "Burning Fang", "Rally", "Forsaken", "Composure", "Ragebrew", "Fortitude", "Saturation", "Fracture", "Binding", "Zenith Ray", "Incision", "Pack Mentality", "Overkill", "Cataclysm", "Firebolt", "Feastbound", "Soul Leach", "Deep Cut", "Infernal Surge", "Primed", "Starfall"};
   private static final Map<String, String> KNOWN = knownTraits();
   public static final String PRIMED = "Primed";

   private static Map<String, String> knownTraits() {
      Map<String, String> map = new LinkedHashMap();

      for(String name : TRAIT_NAMES) {
         map.put(name.toLowerCase(Locale.ROOT), name);
      }

      return map;
   }

   public static List<String> knownNames() {
      return new ArrayList(KNOWN.values());
   }

   public static void registrar() {
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
         String text = FavelaDisplays.sanitize(line);
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

   private static String head(String line) {
      if (line.indexOf(187) >= 0) {
         return null;
      } else {
         int colon = line.indexOf(58);
         String head = (colon > 0 ? line.substring(0, colon) : line).trim();
         int start = 0;

         while(start < head.length() && !Character.isLetterOrDigit(head.charAt(start))) {
            ++start;
         }

         head = head.substring(start).trim();
         return head.isEmpty() ? null : head;
      }
   }

   public static List<String> traits(ItemStack stack) {
      List<String> traits = new ArrayList();

      for(String line : cleanLines(stack)) {
         String head = head(line);
         if (head != null) {
            String name = (String)KNOWN.get(head);
            if (name != null && !traits.contains(name)) {
               traits.add(name);
            }
         }
      }

      return traits;
   }

   public static List<String> unknownHeads(ItemStack stack) {
      List<String> heads = new ArrayList();

      for(String line : cleanLines(stack)) {
         int colon = line.indexOf(58);
         if (colon > 0) {
            String head = head(line);
            if (head != null && !KNOWN.containsKey(head) && !heads.contains(head)) {
               heads.add(head);
            }
         }
      }

      return heads;
   }

   public static boolean hasTrait(ItemStack stack, String name) {
      for(String trait : traits(stack)) {
         if (trait.equalsIgnoreCase(name)) {
            return true;
         }
      }

      return false;
   }

}

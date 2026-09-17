package com.favelaaddons;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;

public class FavelaFonts {
   private static final Map<String, Map<Integer, String>> CACHE = new HashMap();

   public static void forget() {
      CACHE.clear();
   }

   public static String fontName(Style style) {
      FontDescription font = style.getFont();
      if (font instanceof FontDescription.Resource) {
         return ((FontDescription.Resource)font).id().toString();
      } else {
         return "minecraft:default";
      }
   }

   public static String texture(String font, int glyph) {
      return (String)glyphs(font).get(glyph);
   }

   public static boolean textureMatches(String font, int glyph, String phrase) {
      String texture = texture(font, glyph);
      return texture != null && phrase != null && texture.toLowerCase(Locale.ROOT).contains(phrase.toLowerCase(Locale.ROOT));
   }

   private static Map<Integer, String> glyphs(String font) {
      return (Map)CACHE.computeIfAbsent(font, FavelaFonts::read);
   }

   private static Map<Integer, String> read(String font) {
      Map<Integer, String> glyphs = new HashMap();
      Identifier id = Identifier.tryParse(font);
      if (id == null) {
         return glyphs;
      } else {
         Identifier path = Identifier.fromNamespaceAndPath(id.getNamespace(), "font/" + id.getPath() + ".json");
         Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(path);
         if (resource.isEmpty()) {
            return glyphs;
         } else {
            try {
               BufferedReader reader = ((Resource)resource.get()).openAsReader();

               try {
                  JsonElement root = JsonParser.parseReader(reader);
                  JsonArray providers = root.getAsJsonObject().getAsJsonArray("providers");
                  if (providers != null) {
                     for(JsonElement element : providers) {
                        readProvider(element.getAsJsonObject(), glyphs);
                     }
                  }
               } finally {
                  reader.close();
               }
            } catch (Exception var8) {
            }

            return glyphs;
         }
      }
   }

   private static void readProvider(JsonObject provider, Map<Integer, String> glyphs) {
      JsonElement type = provider.get("type");
      if (type != null && "bitmap".equals(type.getAsString())) {
         JsonElement file = provider.get("file");
         JsonArray rows = provider.getAsJsonArray("chars");
         if (file != null && rows != null) {
            String texture = file.getAsString();

            for(JsonElement row : rows) {
               row.getAsString().codePoints().forEach((codepoint) -> {
                  if (codepoint != 32 && codepoint != 0) {
                     glyphs.putIfAbsent(codepoint, texture);
                  }

               });
            }

         }
      }
   }
}

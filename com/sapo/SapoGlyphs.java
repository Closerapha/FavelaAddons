package com.sapo;

public class SapoGlyphs {
   private static final int SPACE = 90057;
   private static final int DIGIT_START = 90060;
   private static final int DIGIT_END = 90069;
   private static final int LOW_START = 90070;
   private static final int LOW_END = 90075;
   private static final int LETTER_G = 90076;
   private static final int HIGH_START = 90081;
   private static final int HIGH_END = 90100;
   private static final int FRAME_LEFT = 90101;
   private static final int FRAME_RIGHT = 90102;

   public static boolean isGlyphText(String text) {
      if (text == null || text.isEmpty()) {
         return false;
      } else {
         int hits = 0;

         for(int cp : text.codePoints().toArray()) {
            if (cp >= SPACE && cp <= FRAME_RIGHT) {
               ++hits;
            }
         }

         return hits >= 2;
      }
   }

   public static String decode(String text) {
      if (text == null) {
         return "";
      } else {
         StringBuilder out = new StringBuilder();

         for(int cp : text.codePoints().toArray()) {
            if (cp == SPACE) {
               out.append(' ');
            } else if (cp == LETTER_G) {
               out.append('g');
            } else if (cp >= DIGIT_START && cp <= DIGIT_END) {
               out.append((char)(48 + cp - DIGIT_START));
            } else if (cp >= LOW_START && cp <= LOW_END) {
               out.append((char)(97 + cp - LOW_START));
            } else if (cp >= HIGH_START && cp <= HIGH_END) {
               out.append((char)(103 + cp - HIGH_START));
            } else if (cp >= 32 && cp < 55296) {
               out.appendCodePoint(cp);
            }
         }

         return out.toString().trim();
      }
   }
}

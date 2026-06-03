package org.example.structural;

import java.util.HashMap;
import java.util.Map;

/**
 * Flyweight — shares common state among many fine-grained objects to save memory.
 * Example: text editor reuses character glyph objects (intrinsic: font/style)
 * while position is extrinsic (passed at render time).
 */
public class Flyweight {

    // Intrinsic (shared) state
    static class CharacterGlyph {
        private final char   symbol;
        private final String font;
        private final int    size;

        CharacterGlyph(char symbol, String font, int size) {
            this.symbol = symbol;
            this.font   = font;
            this.size   = size;
        }

        public void render(int x, int y) {
            System.out.printf("'%c' font=%s size=%d at (%d,%d)%n", symbol, font, size, x, y);
        }
    }

    static class GlyphFactory {
        private final Map<String, CharacterGlyph> cache = new HashMap<>();

        public CharacterGlyph get(char symbol, String font, int size) {
            String key = symbol + font + size;
            return cache.computeIfAbsent(key, k -> {
                System.out.println("Creating glyph for '" + symbol + "'");
                return new CharacterGlyph(symbol, font, size);
            });
        }

        public int cacheSize() { return cache.size(); }
    }

    public static void main(String[] args) {
        GlyphFactory factory = new GlyphFactory();

        String text = "HELLO";
        for (int i = 0; i < text.length(); i++) {
            factory.get(text.charAt(i), "Arial", 12).render(i * 10, 0);
        }

        System.out.println("Unique glyphs created: " + factory.cacheSize()); // 4 (L reused)
    }
}

package com.noahtnt2009.gallifreyan_chronicles.client.watermark;

public final class WatermarkRenderer {
    public static final float STRENGTH = 0.13f;

    private WatermarkRenderer() {
    }

    public static void apply(PixelSurface image, String text, long seed) {
        apply(image, text, seed, STRENGTH);
    }

    public static void apply(PixelSurface image, String text, long seed, float strength) {
        int width = image.width();
        int height = image.height();
        if (text.isEmpty() || width <= 0 || height <= 0) return;

        int scale = Math.max(2, Math.round(height / 360f));
        int textWidth = text.length() * WatermarkFont.ADVANCE * scale - scale;
        int textHeight = WatermarkFont.GLYPH_HEIGHT * scale;
        int cellWidth = textWidth + 12 * scale;
        int cellHeight = textHeight * 4;

        int startY = -(int) Long.remainderUnsigned(mix(seed, -1), cellHeight);
        int row = 0;
        for (int y = startY; y < height; y += cellHeight, row++) {
            int stagger = (row & 1) * (cellWidth / 2);
            int jitter = (int) Long.remainderUnsigned(mix(seed, row), Math.max(1, cellWidth / 4));
            int startX = ((stagger + jitter) % cellWidth) - cellWidth;
            for (int x = startX; x < width; x += cellWidth) {
                drawText(image, text, x, y, scale, strength);
            }
        }
    }

    private static void drawText(PixelSurface image, String text, int x0, int y0, int scale, float strength) {
        int width = image.width();
        int height = image.height();
        if (y0 + WatermarkFont.GLYPH_HEIGHT * scale <= 0 || y0 >= height) return;

        for (int i = 0; i < text.length(); i++) {
            int gx = x0 + i * WatermarkFont.ADVANCE * scale;
            if (gx + WatermarkFont.GLYPH_WIDTH * scale <= 0 || gx >= width) continue;

            int[] rows = WatermarkFont.glyph(text.charAt(i));
            for (int r = 0; r < WatermarkFont.GLYPH_HEIGHT; r++) {
                for (int c = 0; c < WatermarkFont.GLYPH_WIDTH; c++) {
                    if ((rows[r] & (1 << (WatermarkFont.GLYPH_WIDTH - 1 - c))) == 0) continue;
                    int px = gx + c * scale;
                    int py = y0 + r * scale;
                    for (int dy = 0; dy < scale; dy++) {
                        int y = py + dy;
                        if (y < 0 || y >= height) continue;
                        for (int dx = 0; dx < scale; dx++) {
                            int x = px + dx;
                            if (x < 0 || x >= width) continue;
                            image.setArgb(x, y, tint(image.getArgb(x, y), strength));
                        }
                    }
                }
            }
        }
    }

    private static int tint(int argb, float strength) {
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;
        int luma = (r * 299 + g * 587 + b * 114) / 1000;
        int target = luma > 127 ? 0 : 255;
        r += Math.round((target - r) * strength);
        g += Math.round((target - g) * strength);
        b += Math.round((target - b) * strength);
        return (argb & 0xFF000000) | (r << 16) | (g << 8) | b;
    }

    static long mix(long seed, long n) {
        long z = seed + (n + 1) * 0x9E3779B97F4A7C15L;
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }
}

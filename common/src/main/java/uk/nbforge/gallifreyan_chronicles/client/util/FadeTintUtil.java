package uk.nbforge.gallifreyan_chronicles.client.util;

public final class FadeTintUtil {
    private FadeTintUtil() {
    }

    public static int of(float opacity) {
        float faded = 1.0f - opacity;
        int alpha = Math.round(opacity * 255.0f);
        int red = Math.round((1.0f - 0.25f * faded) * 255.0f);
        int green = Math.round((1.0f - 0.08f * faded) * 255.0f);
        return (alpha << 24) | (red << 16) | (green << 8) | 0xFF;
    }
}

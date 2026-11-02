package com.noahtnt2009.gallifreyan_chronicles.sky;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import java.util.Locale;

public final class HexColor {
    public static final Codec<Integer> CODEC = Codec.STRING.comapFlatMap(HexColor::parse, HexColor::format);

    private HexColor() {
    }

    public static DataResult<Integer> parse(String text) {
        String hex = text.startsWith("#") ? text.substring(1) : text;
        if (hex.length() == 6) hex = "ff" + hex;
        if (hex.length() != 8) {
            return DataResult.error(() -> "Colour must be #RRGGBB or #AARRGGBB: " + text);
        }
        try {
            return DataResult.success((int) Long.parseLong(hex, 16));
        } catch (NumberFormatException e) {
            return DataResult.error(() -> "Invalid colour: " + text);
        }
    }

    public static String format(int argb) {
        if ((argb >>> 24) == 0xFF) {
            return String.format(Locale.ROOT, "#%06x", argb & 0xFFFFFF);
        }
        return String.format(Locale.ROOT, "#%08x", argb);
    }

    public static float red(int argb) {
        return ((argb >> 16) & 0xFF) / 255.0F;
    }

    public static float green(int argb) {
        return ((argb >> 8) & 0xFF) / 255.0F;
    }

    public static float blue(int argb) {
        return (argb & 0xFF) / 255.0F;
    }

    public static float alpha(int argb) {
        return ((argb >>> 24) & 0xFF) / 255.0F;
    }
}

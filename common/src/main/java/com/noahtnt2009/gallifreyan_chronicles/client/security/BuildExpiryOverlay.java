package com.noahtnt2009.gallifreyan_chronicles.client.security;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import java.util.Locale;

public final class BuildExpiryOverlay {
    private static final int MARGIN_X = 4;
    private static final int MARGIN_Y = 3;
    private static final int NORMAL_COLOR = 0xFFFFFFFF;
    private static final int WARNING_COLOR = 0xFFFFAA33;
    private static final int CRITICAL_COLOR = 0xFFFF5555;
    private static final long WARNING_MILLIS = 10L * 60L * 1000L;
    private static final long CRITICAL_MILLIS = 60L * 1000L;

    private BuildExpiryOverlay() {
    }

    public static void render(GuiGraphicsExtractor graphics) {
        long remaining = BuildExpiry.remainingMillis();
        if (remaining < 0L) return;

        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        Component text = Component.translatable("gc.build.expires_in", format(remaining));
        int x = minecraft.getWindow().getGuiScaledWidth() - font.width(text) - MARGIN_X;
        graphics.text(font, text, x, MARGIN_Y, color(remaining), true);
    }

    private static int color(long remaining) {
        if (remaining <= CRITICAL_MILLIS) return CRITICAL_COLOR;
        if (remaining <= WARNING_MILLIS) return WARNING_COLOR;
        return NORMAL_COLOR;
    }

    private static String format(long remaining) {
        long totalSeconds = (remaining + 999L) / 1000L;
        long days = totalSeconds / 86400L;
        long hours = (totalSeconds % 86400L) / 3600L;
        long minutes = (totalSeconds % 3600L) / 60L;
        long seconds = totalSeconds % 60L;
        if (days > 0L) return String.format(Locale.ROOT, "%dd %02d:%02d:%02d", days, hours, minutes, seconds);
        if (hours > 0L) return String.format(Locale.ROOT, "%d:%02d:%02d", hours, minutes, seconds);
        return String.format(Locale.ROOT, "%d:%02d", minutes, seconds);
    }
}

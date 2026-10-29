package com.noahtnt2009.gallifreyan_chronicles.client.watermark;

import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class WatermarkOverlay {
    private static final int ALPHA = 0x10;
    private static final int WHITE = (ALPHA << 24) | 0xFFFFFF;
    private static final int BLACK = (ALPHA << 24);
    private static final int LINE_HEIGHT = 9;

    private static String cachedFor = "";
    private static String label = "";
    private static long seed;

    private WatermarkOverlay() {
    }

    public static void render(GuiGraphicsExtractor graphics) {
        if (!WatermarkIdentity.enabled()) return;

        Minecraft minecraft = Minecraft.getInstance();
        User user = minecraft.getUser();
        String key = user.getName() + user.getProfileId();
        if (!key.equals(cachedFor)) {
            cachedFor = key;
            label = WatermarkIdentity.label(user.getName(), user.getProfileId());
            seed = WatermarkIdentity.seed(label);
        }

        Font font = minecraft.font;
        int width = minecraft.getWindow().getGuiScaledWidth();
        int height = minecraft.getWindow().getGuiScaledHeight();
        int textWidth = font.width(label);
        int cellWidth = textWidth + 28;
        int cellHeight = LINE_HEIGHT * 4;

        int startY = -(int) Long.remainderUnsigned(WatermarkRenderer.mix(seed, -1), cellHeight);
        int row = 0;
        for (int y = startY; y < height; y += cellHeight, row++) {
            int stagger = (row & 1) * (cellWidth / 2);
            int jitter = (int) Long.remainderUnsigned(WatermarkRenderer.mix(seed, row), Math.max(1, cellWidth / 4));
            int startX = ((stagger + jitter) % cellWidth) - cellWidth;
            int col = 0;
            for (int x = startX; x < width; x += cellWidth, col++) {
                graphics.text(font, label, x, y, ((row + col) & 1) == 0 ? WHITE : BLACK);
            }
        }
    }
}

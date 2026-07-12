package com.noahtnt2009.gallifreyan_chronicles.client.hud;

import com.noahtnt2009.gallifreyan_chronicles.platform.Services;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class GCHudRenderer {
    private static final String MOD_VERSION_LABEL = "v0.1.0";

    private static final int TITLE_COLOR = 0xFFFFFFFF;

    private static final int MINECRAFT_COLOR = 0xFFFFFFFF;
    private static final int MC_VERSION_COLOR = 0xFF55FF55;
    private static final int DEV_BUILD_COLOR = 0xFF8B1E1E;

    private static final int NEOFORGE_COLOR = 0xFFFF5C4A;
    private static final int FABRIC_COLOR = 0xFFF2B085;

    private static final int MARGIN_X = 4;
    private static final int MARGIN_Y = 4;
    private static final int LINE_HEIGHT = 10;

    private GCHudRenderer() {
    }

    public static void render(GuiGraphicsExtractor graphics) {
        Font font = Minecraft.getInstance().font;

        String platformName = Services.PLATFORM.getPlatformName();
        int platformColor = "NeoForge".equals(platformName)
                ? NEOFORGE_COLOR
                : FABRIC_COLOR;

        String titleLine = "Gallifreyan Chronicles " + MOD_VERSION_LABEL;
        String minecraftText = "Minecraft ";
        String versionText = "26.2 ";
        String devLine = "Development Build";

        int y = MARGIN_Y;

        graphics.text(font, titleLine, MARGIN_X, y, TITLE_COLOR);
        y += LINE_HEIGHT;

        graphics.text(font, minecraftText, MARGIN_X, y, MINECRAFT_COLOR);

        int versionX = MARGIN_X + font.width(minecraftText);
        graphics.text(font, versionText, versionX, y, MC_VERSION_COLOR);

        int platformX = versionX + font.width(versionText);
        graphics.text(font, platformName, platformX, y, platformColor);

        y += LINE_HEIGHT;

        graphics.text(font, devLine, MARGIN_X, y, DEV_BUILD_COLOR);
    }
}
package com.noahtnt2009.gallifreyan_chronicles.client.hud;

import com.noahtnt2009.gallifreyan_chronicles.config.GCConfig;
import com.noahtnt2009.gallifreyan_chronicles.platform.Services;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class GCHudRenderer {
    private static final int ALPHA = 0x90;

    private static final int TITLE_RGB = 0xFFFFFF;
    private static final int DEV_RGB = 0xE0A458;
    private static final int SEPARATOR_RGB = 0xAAAAAA;
    private static final int MC_VERSION_RGB = 0x55FF55;
    private static final int NEOFORGE_RGB = 0xFF5C4A;
    private static final int FABRIC_RGB = 0xF2B085;

    private static final String MOD_ID = "gallifreyan_chronicles";
    private static final String DEV_MARKER = "-dev";

    private static final int MARGIN_X = 4;
    private static final int MARGIN_Y = 3;

    private static boolean resolved;
    private static String baseVersion = "";
    private static String devSuffix = "";
    private static String minecraftVersion = "";
    private static String platformName = "";
    private static int platformRgb = FABRIC_RGB;

    private GCHudRenderer() {
    }

    private static void resolve() {
        if (resolved) return;
        resolved = true;

        String version = Services.PLATFORM.getModVersion(MOD_ID);
        int devIndex = version.indexOf(DEV_MARKER);
        baseVersion = devIndex < 0 ? version : version.substring(0, devIndex);
        devSuffix = devIndex < 0 ? "" : version.substring(devIndex);

        minecraftVersion = Services.PLATFORM.getModVersion("minecraft");
        platformName = Services.PLATFORM.getPlatformName();
        platformRgb = "NeoForge".equals(platformName) ? NEOFORGE_RGB : FABRIC_RGB;
    }

    public static void render(GuiGraphicsExtractor graphics) {
        if (!GCConfig.showVersionHud()) return;

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.debugEntries.isOverlayVisible()) return;

        resolve();

        Font font = minecraft.font;
        int x = MARGIN_X;
        int y = MARGIN_Y;

        x = draw(graphics, font, "GC v" + baseVersion, x, y, TITLE_RGB);
        x = draw(graphics, font, devSuffix, x, y, DEV_RGB);
        x = draw(graphics, font, " | ", x, y, SEPARATOR_RGB);
        x = draw(graphics, font, minecraftVersion, x, y, MC_VERSION_RGB);
        draw(graphics, font, " " + platformName, x, y, platformRgb);
    }

    private static int draw(GuiGraphicsExtractor graphics, Font font, String text, int x, int y, int rgb) {
        if (text.isEmpty()) return x;
        graphics.text(font, text, x, y, (ALPHA << 24) | rgb);
        return x + font.width(text);
    }
}

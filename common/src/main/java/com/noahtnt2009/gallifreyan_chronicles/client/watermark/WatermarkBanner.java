package com.noahtnt2009.gallifreyan_chronicles.client.watermark;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.platform.Services;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class WatermarkBanner {
    private static final int RED = 0xFFFF3030;
    private static final int MARGIN_Y = 4;
    private static final String MOD_ID = "gallifreyan_chronicles";
    private static final String DEV_MARKER = "-dev";

    private static String text;
    private static boolean logged;

    private WatermarkBanner() {
    }

    private static String text() {
        if (text == null) {
            String version = Services.PLATFORM.getModVersion(MOD_ID);
            int devIndex = version.indexOf(DEV_MARKER);
            String baseVersion = devIndex < 0 ? version : version.substring(0, devIndex);
            text = "BUILD " + WatermarkIdentity.buildId()
                    + " | Prototype Build v" + baseVersion + ", Everything subject to change";
        }
        return text;
    }

    public static void render(GuiGraphicsExtractor graphics) {
        if (!WatermarkIdentity.bannerEnabled()) return;

        if (!logged) {
            logged = true;
            Constants.LOG.info("[GC] F1 banner is being drawn");
        }

        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        String banner = text();
        int x = (minecraft.getWindow().getGuiScaledWidth() - font.width(banner)) / 2;
        graphics.text(font, banner, x, MARGIN_Y, RED);
    }
}

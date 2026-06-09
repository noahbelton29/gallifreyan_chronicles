package com.noahtnt2009.gallifreyan_chronicles.client.overlay;

import com.noahtnt2009.gallifreyan_chronicles.GallifreyanChronicles;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

public class HudOverlay {

    private static final Identifier LAYER_ID =
            Identifier.fromNamespaceAndPath(GallifreyanChronicles.MOD_ID, "version_hud");

    private static final int WHITE  = 0xFFFFFFFF;
    private static final int CYAN   = 0xFF55FFFF;
    private static final int YELLOW = 0xFFFFFF55;
    private static final int RED    = 0xFFFF5555;
    private static final int ORANGE = 0xFFFFAA00;

    private static final int LINE_HEIGHT = 10;
    private static final int X = 4;
    private static final int Y = 4;

    public static void init() {
        HudElementRegistry.attachElementAfter(VanillaHudElements.CHAT, LAYER_ID,
                (graphics, deltaTracker) -> {
                    Minecraft mc = Minecraft.getInstance();
                    if (mc.player == null) return;

                    int fps = mc.getFps();
                    double px = mc.player.getX();
                    double py = mc.player.getY();
                    double pz = mc.player.getZ();

                    int y = Y;

                    graphics.text(mc.font, "Gallifreyan Chronicles", X, y, WHITE, true);
                    y += LINE_HEIGHT;

                    int cursor = X;
                    cursor = drawSegment(graphics, mc, "[", cursor, y, WHITE);
                    cursor = drawSegment(graphics, mc, "DEV", cursor, y, CYAN);
                    cursor = drawSegment(graphics, mc, "] ", cursor, y, WHITE);
                    cursor = drawSegment(graphics, mc, "26.1.2", cursor, y, YELLOW);
                    drawSegment(graphics, mc, " Fabric", cursor, y, RED);
                    y += LINE_HEIGHT;

                    graphics.text(mc.font, "FPS: " + fps, X, y, WHITE, true);
                    y += LINE_HEIGHT;

                    graphics.text(mc.font,
                            String.format("XYZ: %.1f / %.1f / %.1f", px, py, pz),
                            X, y, WHITE, true);
                    y += LINE_HEIGHT;

                    graphics.text(mc.font, "! Dev build, expect bugs", X, y, ORANGE, true);
                });
    }

    private static int drawSegment(GuiGraphicsExtractor graphics, Minecraft mc, String text, int x, int y, int color) {
        graphics.text(mc.font, text, x, y, color, true);
        return x + mc.font.width(text);
    }
}
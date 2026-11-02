package com.noahtnt2009.gallifreyan_chronicles.client.hud;

import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisConsoleBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.config.GCConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public final class GCPowerDarkness {
    private static final float MAX_ALPHA = 0.88F;
    private static final float SHADER_MAX_ALPHA = 0.4F;
    public static final float SHADER_BLINDNESS_SCALE = 0.6F;
    private static final float FADE_SECONDS = 1.0F;
    private static final long SNAP_WINDOW_NANOS = 2_000_000_000L;

    private static final List<TardisConsoleBlockEntity> CONSOLES = new ArrayList<>();
    private static float darkness = 0.0F;
    private static long lastNanos = 0L;
    private static long snapUntilNanos = 0L;
    private static Level lastLevel;

    private GCPowerDarkness() {
    }

    public static void track(TardisConsoleBlockEntity console) {
        if (!CONSOLES.contains(console)) {
            CONSOLES.add(console);
            snapUntilNanos = System.nanoTime() + SNAP_WINDOW_NANOS;
        }
    }

    public static void reset() {
        CONSOLES.clear();
        darkness = 0.0F;
        lastNanos = 0L;
        snapUntilNanos = 0L;
        lastLevel = null;
    }

    private static boolean shouldBeDark(Minecraft minecraft) {
        boolean dark = false;
        CONSOLES.removeIf(console -> console.isRemoved() || console.getLevel() != minecraft.level);
        for (TardisConsoleBlockEntity console : CONSOLES) {
            if (!console.hasPower()) dark = true;
        }
        return dark;
    }

    public static float update() {
        Minecraft minecraft = Minecraft.getInstance();
        long now = System.nanoTime();
        float seconds = lastNanos == 0L ? 0.0F : Math.min((now - lastNanos) / 1.0E9F, 0.1F);
        lastNanos = now;

        if (minecraft.level != lastLevel) {
            lastLevel = minecraft.level;
            darkness = 0.0F;
            snapUntilNanos = now + SNAP_WINDOW_NANOS;
        }

        float target = minecraft.level != null && minecraft.player != null && shouldBeDark(minecraft) ? 1.0F : 0.0F;
        if (target < darkness || now >= snapUntilNanos) {
            float step = seconds / FADE_SECONDS;
            darkness = darkness < target ? Math.min(target, darkness + step) : Math.max(target, darkness - step);
        } else {
            darkness = target;
        }

        return darkness * (float) GCConfig.powerDarknessStrength();
    }

    public static void render(GuiGraphicsExtractor graphics) {
        Minecraft minecraft = Minecraft.getInstance();
        float level = update();
        if (level <= 0.0F) return;
        float maxAlpha = ShaderPackDetector.isShaderPackInUse() ? SHADER_MAX_ALPHA : MAX_ALPHA;
        int alpha = (int) (level * maxAlpha * 255.0F);
        graphics.fill(0, 0, minecraft.getWindow().getGuiScaledWidth(), minecraft.getWindow().getGuiScaledHeight(), alpha << 24);
    }
}

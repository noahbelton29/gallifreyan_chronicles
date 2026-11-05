package com.noahtnt2009.gallifreyan_chronicles.client.hud;

import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisConsoleBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.config.GCConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public final class GCPowerDarkness {
    private static final float MAX_ALPHA = 0.88F;
    private static final float SHADER_MAX_ALPHA = 0.4F;
    public static final float SHADER_BLINDNESS_SCALE = 0.6F;
    private static final float FADE_SECONDS = 1.0F;
    private static final long SNAP_WINDOW_NANOS = 2_000_000_000L;

    private static final List<TardisConsoleBlockEntity> CONSOLES = new ArrayList<>();
    private static volatile float darkness = 0.0F;
    private static long lastNanos = 0L;
    private static long snapUntilNanos = 0L;
    private static Level lastLevel;
    private static final Map<Level, RemoteDarkness> REMOTE = new WeakHashMap<>();

    private static final class RemoteDarkness {
        private float darkness;
        private long lastNanos;
    }

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
        REMOTE.clear();
    }

    private static Level playerLevel(Minecraft minecraft) {
        return minecraft.player != null ? minecraft.player.level() : minecraft.level;
    }

    private static boolean shouldBeDark(Level level) {
        boolean dark = false;
        CONSOLES.removeIf(TardisConsoleBlockEntity::isRemoved);
        for (TardisConsoleBlockEntity console : CONSOLES) {
            if (console.getLevel() == level && !console.hasPower()) dark = true;
        }
        return dark;
    }

    public static float powerOffAmount() {
        return darkness;
    }

    public static float update() {
        Minecraft minecraft = Minecraft.getInstance();
        long now = System.nanoTime();
        float seconds = lastNanos == 0L ? 0.0F : Math.min((now - lastNanos) / 1.0E9F, 0.1F);
        lastNanos = now;

        Level level = playerLevel(minecraft);
        if (level != lastLevel) {
            lastLevel = level;
            darkness = 0.0F;
            snapUntilNanos = now + SNAP_WINDOW_NANOS;
        }

        float target = level != null && minecraft.player != null && shouldBeDark(level) ? 1.0F : 0.0F;
        if (target < darkness || now >= snapUntilNanos) {
            float step = seconds / FADE_SECONDS;
            darkness = darkness < target ? Math.min(target, darkness + step) : Math.max(target, darkness - step);
        } else {
            darkness = target;
        }

        return darkness * (float) GCConfig.powerDarknessStrength();
    }

    public static float darknessOf(Level level) {
        if (level == null) return 0.0F;
        Minecraft minecraft = Minecraft.getInstance();
        if (level == playerLevel(minecraft)) return update();

        long now = System.nanoTime();
        float target = minecraft.player != null && shouldBeDark(level) ? 1.0F : 0.0F;
        RemoteDarkness remote = REMOTE.get(level);
        if (remote == null) {
            remote = new RemoteDarkness();
            remote.darkness = target;
            REMOTE.put(level, remote);
        } else {
            float seconds = Math.min((now - remote.lastNanos) / 1.0E9F, 0.1F);
            float step = seconds / FADE_SECONDS;
            remote.darkness = remote.darkness < target
                    ? Math.min(target, remote.darkness + step)
                    : Math.max(target, remote.darkness - step);
        }
        remote.lastNanos = now;
        return remote.darkness * (float) GCConfig.powerDarknessStrength();
    }

    public static float renderLevelDarkness() {
        return darknessOf(Minecraft.getInstance().level);
    }

    public static float overlayMaxAlpha() {
        return ShaderPackDetector.isShaderPackInUse() ? SHADER_MAX_ALPHA : MAX_ALPHA;
    }

    public static void render(GuiGraphicsExtractor graphics) {
        Minecraft minecraft = Minecraft.getInstance();
        float level = update();
        if (level <= 0.0F) return;
        float maxAlpha = overlayMaxAlpha();
        int alpha = (int) (level * maxAlpha * 255.0F);
        graphics.fill(0, 0, minecraft.getWindow().getGuiScaledWidth(), minecraft.getWindow().getGuiScaledHeight(), alpha << 24);
    }
}

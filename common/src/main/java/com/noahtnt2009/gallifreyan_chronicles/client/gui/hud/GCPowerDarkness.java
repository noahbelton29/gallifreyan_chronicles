package com.noahtnt2009.gallifreyan_chronicles.client.gui.hud;

import com.noahtnt2009.gallifreyan_chronicles.util.TardisDimensions;
import com.noahtnt2009.gallifreyan_chronicles.client.tardis.RematCatchClient;
import com.noahtnt2009.gallifreyan_chronicles.client.tardis.TardisFrightClient;
import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.block.ConsoleBlock;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisConsoleBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.config.GCConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public final class GCPowerDarkness {
    private static final float MAX_ALPHA = 0.88F;
    public static final int ALARM_RED = 0xFF1010;
    public static final float ALARM_MAX_ALPHA = 0.55F;
    private static final float SHADER_MAX_ALPHA = 0.4F;
    public static final float SHADER_BLINDNESS_SCALE = 0.6F;
    private static final float FADE_SECONDS = 1.0F;
    private static final long SNAP_WINDOW_NANOS = 2_000_000_000L;
    private static final long SHADER_READ_WINDOW_NANOS = 1_000_000_000L;

    private static final List<TardisConsoleBlockEntity> CONSOLES = new ArrayList<>();
    private static volatile float darkness = 0.0F;
    private static long snapUntilNanos = 0L;
    private static volatile long shaderBlindnessReadNanos = 0L;
    private static final Set<Level> NO_CONSOLE_LEVELS = Collections.newSetFromMap(new WeakHashMap<>());
    private static final Map<Level, LevelDarkness> STATES = new WeakHashMap<>();

    private static final class LevelDarkness {
        private float darkness;
        private long lastNanos;
    }

    private GCPowerDarkness() {
    }

    public static void track(TardisConsoleBlockEntity console) {
        if (!CONSOLES.contains(console)) {
            CONSOLES.add(console);
            if (console.getLevel() != null) NO_CONSOLE_LEVELS.remove(console.getLevel());
            snapUntilNanos = System.nanoTime() + SNAP_WINDOW_NANOS;
        }
    }

    public static void reset() {
        GCDangerAlarm.reset();
        TardisFrightClient.reset();
        CONSOLES.clear();
        NO_CONSOLE_LEVELS.clear();
        darkness = 0.0F;
        snapUntilNanos = 0L;
        shaderBlindnessReadNanos = 0L;
        STATES.clear();
    }

    private static Level playerLevel(Minecraft minecraft) {
        return minecraft.player != null ? minecraft.player.level() : minecraft.level;
    }

    /* only tardis interiors go dark. a console placed and broken outside
       (or left unpowered outside) must not darken the normal world */
    private static boolean isTardisLevel(Level level) {
        return TardisDimensions.isInterior(level);
    }

    private static boolean shouldBeDark(Level level) {
        if (!isTardisLevel(level)) return false;
        /* materialising around you hides the console for a moment */
        if (RematCatchClient.hidesBlocksIn(level)) return false;
        boolean dark = false;
        boolean hasConsole = false;
        for (TardisConsoleBlockEntity console : new ArrayList<>(CONSOLES)) {
            if (!console.isRemoved()) continue;
            CONSOLES.remove(console);
            // only a destroyed console counts, one that was just unloaded with its chunk is still there
            Level consoleLevel = console.getLevel();
            if (consoleLevel != null && consoleLevel.hasChunkAt(console.getBlockPos())
                    && isTardisLevel(consoleLevel)
                    && !(consoleLevel.getBlockState(console.getBlockPos()).getBlock() instanceof ConsoleBlock)) {
                NO_CONSOLE_LEVELS.add(consoleLevel);
            }
        }
        for (TardisConsoleBlockEntity console : CONSOLES) {
            if (console.getLevel() != level) continue;
            hasConsole = true;
            if (!console.hasPower()) dark = true;
        }
        if (hasConsole) NO_CONSOLE_LEVELS.remove(level);
        return dark || (!hasConsole && NO_CONSOLE_LEVELS.contains(level));
    }

    public static float powerOffAmount() {
        return darkness;
    }

    /* moves the fade for one world along by the real time that passed */
    private static float step(Level level, long now) {
        Minecraft minecraft = Minecraft.getInstance();
        float target = minecraft.player != null && shouldBeDark(level) ? 1.0F : 0.0F;
        LevelDarkness state = STATES.get(level);
        if (state == null) {
            /* first look at this world: start where it should be, no fade */
            state = new LevelDarkness();
            state.darkness = target;
            state.lastNanos = now;
            STATES.put(level, state);
            return state.darkness;
        }
        if (now < snapUntilNanos && target > state.darkness) {
            /* a console just loaded in (joining the world): go dark straight away */
            state.darkness = target;
        } else {
            float seconds = Math.min((now - state.lastNanos) / 1.0E9F, FADE_SECONDS);
            float stepSize = seconds / FADE_SECONDS;
            state.darkness = state.darkness < target
                    ? Math.min(target, state.darkness + stepSize)
                    : Math.max(target, state.darkness - stepSize);
        }
        state.lastNanos = now;
        return state.darkness;
    }

    public static float update() {
        Level level = playerLevel(Minecraft.getInstance());
        darkness = level == null ? 0.0F : step(level, System.nanoTime());
        float lights = Math.max(darkness, GCDangerAlarm.amountFor(level));
        return Math.max(lights, TardisFrightClient.flickerOf(level)) * (float) GCConfig.powerDarknessStrength();
    }

    public static float darknessOf(Level level) {
        if (level == null) return 0.0F;
        float lights = Math.max(step(level, System.nanoTime()), GCDangerAlarm.amountFor(level));
        return Math.max(lights, TardisFrightClient.flickerOf(level)) * (float) GCConfig.powerDarknessStrength();
    }

    /* just the power-off darkness, no alarm */
    public static float powerDarknessOf(Level level) {
        if (level == null) return 0.0F;
        return step(level, System.nanoTime()) * (float) GCConfig.powerDarknessStrength();
    }

    public static float alarmOf(Level level) {
        return GCDangerAlarm.amountFor(level) * (float) GCConfig.powerDarknessStrength();
    }

    public static float renderLevelDarkness() {
        return darknessOf(Minecraft.getInstance().level);
    }

    public static void noteShaderBlindnessRead() {
        shaderBlindnessReadNanos = System.nanoTime();
    }

    public static boolean shaderPackHandlesDarkness() {
        long read = shaderBlindnessReadNanos;
        return read != 0L && System.nanoTime() - read < SHADER_READ_WINDOW_NANOS && ShaderPackDetector.isShaderPackInUse();
    }

    public static float overlayMaxAlpha() {
        return ShaderPackDetector.isShaderPackInUse() ? SHADER_MAX_ALPHA : MAX_ALPHA;
    }

    public static void render(GuiGraphicsExtractor graphics) {
        Minecraft minecraft = Minecraft.getInstance();
        float level = update();
        if (level <= 0.0F) return;
        if (shaderPackHandlesDarkness()) return;
        float maxAlpha = overlayMaxAlpha();
        boolean alarm = GCDangerAlarm.amountFor(playerLevel(minecraft)) > darkness;
        int alpha = (int) (level * (alarm ? ALARM_MAX_ALPHA : maxAlpha) * 255.0F);
        int colour = alarm ? ALARM_RED : 0;
        graphics.fill(0, 0, minecraft.getWindow().getGuiScaledWidth(), minecraft.getWindow().getGuiScaledHeight(), (alpha << 24) | colour);
    }
}

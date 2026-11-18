package uk.nbforge.gallifreyan_chronicles.client.gui.hud;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.level.Level;
import uk.nbforge.gallifreyan_chronicles.client.compat.iris.IrisCompat;
import uk.nbforge.gallifreyan_chronicles.client.lifecycle.ClientState;
import uk.nbforge.gallifreyan_chronicles.client.tardis.RematCatchClient;
import uk.nbforge.gallifreyan_chronicles.client.tardis.TardisFrightClient;
import uk.nbforge.gallifreyan_chronicles.config.GCConfig;
import uk.nbforge.gallifreyan_chronicles.core.block.ConsoleBlock;
import uk.nbforge.gallifreyan_chronicles.core.block_entity.TardisConsoleBlockEntity;
import uk.nbforge.gallifreyan_chronicles.tardis.TardisDimensionUtil;

public final class PowerDarkness {
    private static final float MAX_ALPHA = 0.88f;
    public static final int ALARM_RED = 0xFF1010;
    public static final float ALARM_MAX_ALPHA = 0.55f;
    private static final float SHADER_MAX_ALPHA = 0.4f;
    public static final float SHADER_BLINDNESS_SCALE = 0.6f;
    private static final float FADE_SECONDS = 1.0f;
    private static final long SNAP_WINDOW_NANOS = 2_000_000_000L;
    private static final long SHADER_READ_WINDOW_NANOS = 1_000_000_000L;

    private static final List<TardisConsoleBlockEntity> CONSOLES = ClientState.track(new ArrayList<>());
    private static volatile float darkness = 0.0f;
    private static long snapUntilNanos = 0L;
    private static volatile long shaderBlindnessReadNanos = 0L;
    private static final Set<Level> NO_CONSOLE_LEVELS =
            ClientState.track(Collections.newSetFromMap(new WeakHashMap<>()));
    private static final Map<Level, LevelDarkness> STATES = ClientState.track(new WeakHashMap<>());

    private static final class LevelDarkness {
        private float darkness;
        private long lastNanos;
    }

    private PowerDarkness() {}

    public static void track(TardisConsoleBlockEntity console) {
        if (!CONSOLES.contains(console)) {
            CONSOLES.add(console);
            if (console.getLevel() != null) NO_CONSOLE_LEVELS.remove(console.getLevel());
            snapUntilNanos = System.nanoTime() + SNAP_WINDOW_NANOS;
        }
    }

    public static void reset() {
        DangerAlarm.reset();
        TardisFrightClient.reset();
        CONSOLES.clear();
        NO_CONSOLE_LEVELS.clear();
        darkness = 0.0f;
        snapUntilNanos = 0L;
        shaderBlindnessReadNanos = 0L;
        STATES.clear();
    }

    private static Level playerLevel(Minecraft minecraft) {
        return minecraft.player != null ? minecraft.player.level() : minecraft.level;
    }

    /** a console placed and broken or left unpowered outside shouldnt darken the normal world */
    private static boolean isTardisLevel(Level level) {
        return TardisDimensionUtil.isInterior(level);
    }

    private static boolean shouldBeDark(Level level) {
        if (!isTardisLevel(level)) return false;
        if (RematCatchClient.hidesBlocksIn(level)) return false;
        boolean dark = false;
        boolean hasConsole = false;
        for (TardisConsoleBlockEntity console : new ArrayList<>(CONSOLES)) {
            if (!console.isRemoved()) continue;
            CONSOLES.remove(console);
            Level consoleLevel = console.getLevel();
            if (consoleLevel != null
                    && consoleLevel.isLoaded(console.getBlockPos())
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

    private static float step(Level level, long now) {
        Minecraft minecraft = Minecraft.getInstance();
        float target = minecraft.player != null && shouldBeDark(level) ? 1.0f : 0.0f;
        LevelDarkness state = STATES.get(level);
        if (state == null) {
            state = new LevelDarkness();
            state.darkness = target;
            state.lastNanos = now;
            STATES.put(level, state);
            return state.darkness;
        }
        if (now < snapUntilNanos && target > state.darkness) {
            state.darkness = target;
        } else {
            float seconds = Math.min((now - state.lastNanos) / 1.0E9f, FADE_SECONDS);
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
        darkness = level == null ? 0.0f : step(level, System.nanoTime());
        float lights = Math.max(darkness, DangerAlarm.amountFor(level));
        return Math.max(lights, TardisFrightClient.flickerOf(level)) * (float) GCConfig.powerDarknessStrength();
    }

    public static float darknessOf(Level level) {
        if (level == null) return 0.0f;
        float lights = Math.max(step(level, System.nanoTime()), DangerAlarm.amountFor(level));
        return Math.max(lights, TardisFrightClient.flickerOf(level)) * (float) GCConfig.powerDarknessStrength();
    }

    public static float powerDarknessOf(Level level) {
        if (level == null) return 0.0f;
        return step(level, System.nanoTime()) * (float) GCConfig.powerDarknessStrength();
    }

    public static float alarmOf(Level level) {
        return DangerAlarm.amountFor(level) * (float) GCConfig.powerDarknessStrength();
    }

    public static float renderLevelDarkness() {
        return darknessOf(Minecraft.getInstance().level);
    }

    public static void noteShaderBlindnessRead() {
        shaderBlindnessReadNanos = System.nanoTime();
    }

    public static boolean shaderPackHandlesDarkness() {
        long read = shaderBlindnessReadNanos;
        return read != 0L && System.nanoTime() - read < SHADER_READ_WINDOW_NANOS && IrisCompat.shaderPackInUse();
    }

    public static float overlayMaxAlpha() {
        return IrisCompat.shaderPackInUse() ? SHADER_MAX_ALPHA : MAX_ALPHA;
    }

    public static void render(GuiGraphicsExtractor graphics) {
        Minecraft minecraft = Minecraft.getInstance();
        float level = update();
        if (level <= 0.0f) return;
        if (shaderPackHandlesDarkness()) return;
        float maxAlpha = overlayMaxAlpha();
        boolean alarm = DangerAlarm.amountFor(playerLevel(minecraft)) > darkness;
        int alpha = (int) (level * (alarm ? ALARM_MAX_ALPHA : maxAlpha) * 255.0f);
        int colour = alarm ? ALARM_RED : 0;
        graphics.fill(
                0,
                0,
                minecraft.getWindow().getGuiScaledWidth(),
                minecraft.getWindow().getGuiScaledHeight(),
                (alpha << 24) | colour);
    }
}

package uk.nbforge.gallifreyan_chronicles.client.tardis;

import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public final class TardisFrightClient {
    private static final long SLOT_MS = 500L;
    private static final long FADE_IN_NANOS = 500_000_000L;
    private static final long FADE_OUT_NANOS = 2_000_000_000L;
    private static final float MAX_DIP = 0.85f;

    private static volatile @Nullable State state;

    private static final class State {
        final ResourceKey<Level> dimension;
        final float severity;
        final long startNanos;
        final long endNanos;
        final long seed;

        State(ResourceKey<Level> dimension, float severity, long startNanos, long endNanos) {
            this.dimension = dimension;
            this.severity = severity;
            this.startNanos = startNanos;
            this.endNanos = endNanos;
            this.seed = startNanos * 0x9E3779B97F4A7C15L;
        }
    }

    private TardisFrightClient() {}

    public static void start(float severity, int ticks, ResourceKey<Level> dimension) {
        long now = System.nanoTime();
        state = new State(dimension, Mth.clamp(severity, 0f, 1f), now, now + ticks * 50_000_000L);
    }

    public static void reset() {
        state = null;
    }

    private static @Nullable State active(@Nullable Level level) {
        State current = state;
        if (current == null || level == null) return null;
        if (System.nanoTime() >= current.endNanos) {
            state = null;
            return null;
        }
        return current.dimension.equals(level.dimension()) ? current : null;
    }

    private static float envelope(State s, long now) {
        float in = Mth.clamp((now - s.startNanos) / (float) FADE_IN_NANOS, 0f, 1f);
        float out = Mth.clamp((s.endNanos - now) / (float) FADE_OUT_NANOS, 0f, 1f);
        return Math.min(in, out);
    }

    public static float flickerOf(@Nullable Level level) {
        State s = active(level);
        if (s == null) return 0f;

        long now = System.nanoTime();
        long elapsedMs = (now - s.startNanos) / 1_000_000L;
        long slot = elapsedMs / SLOT_MS;
        float t = (elapsedMs % SLOT_MS) / (float) SLOT_MS;

        float chance = 0.3f + 0.5f * s.severity;
        if (hash01(s.seed, slot) > chance) return 0f;

        float depth = (0.35f + 0.5f * hash01(s.seed ^ 0x5DEECE66DL, slot)) * (0.5f + 0.5f * s.severity);
        float dip = envelope(s, now) * depth * (float) Math.sin(Math.PI * t);
        return Math.min(MAX_DIP, dip);
    }

    public static float humWarp(@Nullable Level level) {
        State s = active(level);
        return s == null ? 0f : envelope(s, System.nanoTime()) * s.severity;
    }

    public static float warpedPitch(float warp) {
        double t = System.nanoTime() / 1.0E9;
        double wobble = 0.5 + 0.5 * Math.sin(t * 2.0 * Math.PI * 0.7);
        double shiver = Math.sin(t * 2.0 * Math.PI * 3.1) * 0.08;
        float pitch = (float) (1.0 - warp * (0.25 + 0.25 * wobble) + warp * shiver);
        return Mth.clamp(pitch, 0.5f, 1.2f);
    }

    public static float warpedVolume(float warp) {
        double t = System.nanoTime() / 1.0E9;
        float tremble = (float) (0.5 + 0.5 * Math.sin(t * 2.0 * Math.PI * 2.3));
        return 1.0f - 0.35f * warp * tremble;
    }

    private static float hash01(long seed, long n) {
        long h = seed + n * 0x9E3779B97F4A7C15L;
        h ^= (h >>> 30);
        h *= 0xBF58476D1CE4E5B9L;
        h ^= (h >>> 27);
        h *= 0x94D049BB133111EBL;
        h ^= (h >>> 31);
        return (h >>> 40) / (float) (1L << 24);
    }
}

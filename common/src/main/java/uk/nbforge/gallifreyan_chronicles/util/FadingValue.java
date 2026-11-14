package uk.nbforge.gallifreyan_chronicles.util;

public final class FadingValue {
    private static final float MAX_STEP_SECONDS = 0.1F;

    private final float fadeSeconds;
    private float value;
    private boolean initialised;
    private long lastNanos;

    public FadingValue(float fadeSeconds, float initial) {
        this.fadeSeconds = fadeSeconds;
        this.value = initial;
        this.initialised = true;
    }

    private FadingValue(float fadeSeconds) {
        this.fadeSeconds = fadeSeconds;
    }

    public static FadingValue snapping(float fadeSeconds) {
        return new FadingValue(fadeSeconds);
    }

    public float update(float target) {
        long now = System.nanoTime();
        if (!initialised) {
            value = target;
            initialised = true;
        } else {
            float elapsed = lastNanos == 0L ? 0.0F : Math.min((now - lastNanos) / 1.0E9F, MAX_STEP_SECONDS);
            float step = elapsed / fadeSeconds;
            value = value < target ? Math.min(target, value + step) : Math.max(target, value - step);
        }
        lastNanos = now;
        return value;
    }
}

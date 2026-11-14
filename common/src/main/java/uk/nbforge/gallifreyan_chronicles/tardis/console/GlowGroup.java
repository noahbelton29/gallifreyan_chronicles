package uk.nbforge.gallifreyan_chronicles.tardis.console;


public record GlowGroup(String name, String prefix, int frames, int holdTicks, int fadeTicks) {
    public static final int DEFAULT_HOLD_TICKS = 30;
    public static final int DEFAULT_FADE_TICKS = 8;
    private static final float FADE_POWER = 2.5f;

    public static String defaultPrefix(String name) {
        return "glowmask_" + name + "_";
    }

    public float weight(float time, int frame) {
        if (frames <= 1) return 1f;

        float cycleTicks = holdTicks + fadeTicks;
        int active = (int) Math.floor(time / cycleTicks) % frames;
        float local = time % cycleTicks;

        if (local < holdTicks) {
            return frame == active ? 1f : 0f;
        }

        float fade = Math.clamp((local - holdTicks) / fadeTicks, 0f, 1f);

        if (frame == active) return (float) Math.pow(1f - fade, FADE_POWER);
        if (frame == (active + 1) % frames) return (float) Math.pow(fade, FADE_POWER);
        return 0f;
    }
}

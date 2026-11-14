package uk.nbforge.gallifreyan_chronicles.tardis.exterior.fade;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class CloakFade {
    public static final int DURATION_TICKS = 30;
    private static final long NO_START_TICK = Long.MIN_VALUE / 4;

    private boolean cloaked;
    private long startTick = NO_START_TICK;

    public boolean isCloaked() {
        return cloaked;
    }

    public boolean isComplete(long now) {
        return cloaked && now - startTick >= DURATION_TICKS;
    }

    public boolean set(boolean cloaked, long now) {
        if (this.cloaked == cloaked) return false;
        float current = opacity(now, 0.0f);
        this.cloaked = cloaked;
        float progress = cloaked ? 1.0f - current : current;
        this.startTick = now - Math.round(progress * DURATION_TICKS);
        return true;
    }

    public float opacity(long now, float partialTick) {
        float progress = Math.clamp(((now - startTick) + partialTick) / DURATION_TICKS, 0.0f, 1.0f);
        return cloaked ? 1.0f - progress : progress;
    }

    public float restingOpacity() {
        return cloaked ? 0.0f : 1.0f;
    }

    public void copyFrom(CloakFade other) {
        this.cloaked = other.cloaked;
        this.startTick = other.startTick;
    }

    public void save(ValueOutput output) {
        output.putBoolean("Cloaked", cloaked);
        output.putLong("CloakStartTick", startTick);
    }

    public void load(ValueInput input) {
        cloaked = input.getBooleanOr("Cloaked", false);
        startTick = input.getLongOr("CloakStartTick", NO_START_TICK);
    }
}

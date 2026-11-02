package com.noahtnt2009.gallifreyan_chronicles.tardis.exterior;

import com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.fade.FadeType;
import com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.fade.FadeTypeKinds;
import com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.fade.FadeTypes;
import com.noahtnt2009.gallifreyan_chronicles.tardis.flight.TardisFlightState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class ExteriorFlight {
    private static final long NO_FADE_START_TICK = Long.MIN_VALUE / 4;

    private boolean inFlight = false;
    private long fadeStartTick = NO_FADE_START_TICK;
    private long landAtTick = -1L;
    private FadeType fadeType = FadeTypes.DEFAULT;
    private int seenFadeGeneration = -1;
    private TardisFlightState state = TardisFlightState.IDLE;
    private long stateEndTick = 0L;

    public boolean inFlight() {
        return inFlight;
    }

    public FadeType fadeType() {
        return fadeType;
    }

    public long fadeStartTick() {
        return fadeStartTick;
    }

    public TardisFlightState state() {
        return state;
    }

    public long duration() {
        return inFlight ? fadeType.timing().fadeOutTicks() : fadeType.timing().fadeInTicks();
    }

    public boolean isFullyInvisible(long now) {
        return inFlight && now - fadeStartTick >= fadeType.timing().fadeOutTicks();
    }

    public float restingOpacity() {
        return inFlight ? 0.0f : 1.0f;
    }

    public float opacity(long now, float partialTick) {
        float elapsed = (now - fadeStartTick) + partialTick;
        float progress = Math.clamp(elapsed / duration(), 0.0f, 1.0f);
        if (progress >= 1.0f) return restingOpacity();

        float gone = inFlight ? progress : 1.0f - progress;
        return fadeType.opacity(gone, elapsed);
    }

    public boolean setInFlight(boolean inFlight, long now) {
        if (this.inFlight == inFlight) return false;

        float oldProgress = Math.clamp((now - fadeStartTick) / (float) duration(), 0.0f, 1.0f);
        float gone = this.inFlight ? oldProgress : 1.0f - oldProgress;

        this.inFlight = inFlight;
        float newProgress = inFlight ? gone : 1.0f - gone;
        this.fadeStartTick = now - Math.round(newProgress * duration());
        if (inFlight && gone <= 0.0f) {
            this.fadeStartTick = now + fadeType.timing().takeoffDelayTicks();
        }
        return true;
    }

    public boolean scheduleLanding(long now, int delayTicks) {
        if (!inFlight) return false;
        landAtTick = now + delayTicks;
        return true;
    }

    public boolean cancelLanding() {
        if (landAtTick < 0L) return false;
        landAtTick = -1L;
        return true;
    }

    public long consumeDueLanding(long now) {
        if (landAtTick < 0L || now < landAtTick) return -1L;
        long dueTick = landAtTick;
        landAtTick = -1L;
        return dueTick;
    }

    public boolean setFadeType(FadeType fadeType, long now, boolean rebase) {
        if (this.fadeType == fadeType) return false;

        if (rebase && fadeStartTick <= now) {
            float progress = Math.clamp((now - fadeStartTick) / (float) duration(), 0.0f, 1.0f);
            this.fadeType = fadeType;
            this.fadeStartTick = now - Math.round(progress * duration());
            return true;
        }
        this.fadeType = fadeType;
        return true;
    }

    public boolean refreshFadeType(long now) {
        int generation = FadeTypes.generation();
        if (seenFadeGeneration == generation) return false;
        seenFadeGeneration = generation;

        String id = fadeType.id();
        if (!FadeTypes.contains(id)) return false;
        return setFadeType(FadeTypes.get(id), now, true);
    }

    public boolean expireState(long now) {
        if (!state.isTimed() || now < stateEndTick) return false;
        state = state.afterTimer();
        return true;
    }

    public void setState(TardisFlightState state, long now, long durationTicks) {
        this.state = state;
        this.stateEndTick = now + durationTicks;
    }

    public void copyFrom(ExteriorFlight other) {
        this.inFlight = other.inFlight;
        this.fadeStartTick = other.fadeStartTick;
        this.landAtTick = other.landAtTick;
        this.state = other.state;
        this.stateEndTick = other.stateEndTick;
        this.fadeType = other.fadeType;
    }

    public void save(ValueOutput output) {
        output.putBoolean("InFlight", inFlight);
        output.putLong("FadeStartTick", fadeStartTick);
        output.putLong("LandAtTick", landAtTick);
        output.putString("FlightState", state.name());
        output.putLong("FlightStateEnd", stateEndTick);
        output.putString("FadeType", fadeType.id());
        output.store("Fade", FadeTypeKinds.CODEC, fadeType);
    }

    public void load(ValueInput input) {
        inFlight = input.getBooleanOr("InFlight", false);
        fadeStartTick = input.getLongOr("FadeStartTick", NO_FADE_START_TICK);
        landAtTick = input.getLongOr("LandAtTick", -1L);
        state = TardisFlightState.parse(input.getStringOr("FlightState", ""),
                inFlight ? TardisFlightState.IN_VORTEX : TardisFlightState.IDLE);
        stateEndTick = input.getLongOr("FlightStateEnd", 0L);
        String fadeId = input.getStringOr("FadeType", FadeTypes.DEFAULT.id());
        fadeType = input.read("Fade", FadeTypeKinds.CODEC)
                .map(fade -> fade.withId(fadeId))
                .orElseGet(() -> FadeTypes.get(fadeId));
    }
}

package uk.nbforge.gallifreyan_chronicles.tardis.flight;

import uk.nbforge.gallifreyan_chronicles.tardis.exterior.fade.FadeType;
import uk.nbforge.gallifreyan_chronicles.tardis.codec.exterior.fade.FadeTypeKindCodec;
import uk.nbforge.gallifreyan_chronicles.tardis.exterior.fade.FadeTypes;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class ExteriorFlight {
    private static final long NO_FADE_START_TICK = Long.MIN_VALUE / 4;
    private static final float QUICK_FADE_PULSE_DEPTH = 0.65f;

    private boolean inFlight = false;
    private long fadeStartTick = NO_FADE_START_TICK;
    private long landAtTick = -1L;
    private FadeType fadeType = FadeTypes.DEFAULT;
    private int seenFadeGeneration = -1;
    private TardisFlightState state = TardisFlightState.IDLE;
    private long stateEndTick = 0L;
    private int quickFadeInTicks = 0;

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
        if (inFlight) return fadeType.timing().fadeOutTicks();
        return quickFadeInTicks > 0 ? quickFadeInTicks : fadeType.timing().fadeInTicks();
    }

    public void setQuickFadeIn(int ticks) {
        this.quickFadeInTicks = Math.max(0, ticks);
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

        if (!inFlight && quickFadeInTicks > 0) {
            float dip = (float) Math.sin(Math.PI * progress);
            return progress * (1.0f - QUICK_FADE_PULSE_DEPTH * dip * dip);
        }

        float gone = inFlight ? progress : 1.0f - progress;
        return fadeType.opacity(gone, elapsed);
    }

    public boolean setInFlight(boolean inFlight, long now) {
        if (this.inFlight == inFlight) return false;
        if (inFlight) this.quickFadeInTicks = 0;

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

    public void vanish(long now) {
        this.inFlight = true;
        this.quickFadeInTicks = 0;
        this.fadeStartTick = now - fadeType.timing().fadeOutTicks();
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
        this.quickFadeInTicks = other.quickFadeInTicks;
        this.fadeType = other.fadeType;
    }

    public void save(ValueOutput output) {
        output.putBoolean("InFlight", inFlight);
        output.putLong("FadeStartTick", fadeStartTick);
        output.putLong("LandAtTick", landAtTick);
        output.putString("FlightState", state.name());
        output.putLong("FlightStateEnd", stateEndTick);
        output.putInt("QuickFadeIn", quickFadeInTicks);
        output.putString("FadeType", fadeType.id());
        output.store("Fade", FadeTypeKindCodec.CODEC, fadeType);
    }

    public void load(ValueInput input) {
        inFlight = input.getBooleanOr("InFlight", false);
        fadeStartTick = input.getLongOr("FadeStartTick", NO_FADE_START_TICK);
        landAtTick = input.getLongOr("LandAtTick", -1L);
        state = TardisFlightState.parse(input.getStringOr("FlightState", ""),
                inFlight ? TardisFlightState.IN_VORTEX : TardisFlightState.IDLE);
        stateEndTick = input.getLongOr("FlightStateEnd", 0L);
        quickFadeInTicks = input.getIntOr("QuickFadeIn", 0);
        String fadeId = input.getStringOr("FadeType", FadeTypes.DEFAULT.id());
        fadeType = input.read("Fade", FadeTypeKindCodec.CODEC)
                .map(fade -> fade.withId(fadeId))
                .orElseGet(() -> FadeTypes.get(fadeId));
    }
}

package uk.nbforge.gallifreyan_chronicles.tardis.exterior.fade;

import com.mojang.serialization.MapCodec;
import uk.nbforge.gallifreyan_chronicles.tardis.codec.exterior.fade.FadeCodec;

public final class PulseFadeType extends FadeType {
    private final float pulseTicks;
    private final float pulseRamp;
    private final float pulseDepth;
    private static final float MID_DEPTH_CAP = 0.5f;

    public PulseFadeType(String id, FadeTiming timing, float pulseTicks, float pulseRamp, float pulseDepth) {
        super(id, timing);
        this.pulseTicks = pulseTicks;
        this.pulseRamp = pulseRamp;
        this.pulseDepth = pulseDepth;
    }

    public float pulseTicks() {
        return pulseTicks;
    }

    public float pulseRamp() {
        return pulseRamp;
    }

    public float pulseDepth() {
        return pulseDepth;
    }

    @Override
    public MapCodec<PulseFadeType> codec() {
        return FadeCodec.PULSE;
    }

    @Override
    public float opacity(float gone, float elapsed) {
        float wave = 0.5f + 0.5f * (float) Math.cos(elapsed * 2.0 * Math.PI / pulseTicks);
        float cap = MID_DEPTH_CAP + (1.0f - MID_DEPTH_CAP) * gone * gone * gone;
        float depth = Math.min(Math.min(pulseDepth, gone * pulseRamp), cap);
        return (1.0f - gone) * (1.0f - depth * (1.0f - wave));
    }

    @Override
    public PulseFadeType withId(String id) {
        return new PulseFadeType(id, timing(), pulseTicks, pulseRamp, pulseDepth);
    }
}

package com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.fade;

public final class PulseFadeType extends FadeType {
    private static final float PULSE_TICKS = 30.0f;

    public PulseFadeType() {
        super("newwho");
    }

    @Override
    public float opacity(float gone, float elapsed) {
        float wave = 0.5f + 0.5f * (float) Math.cos(elapsed * 2.0 * Math.PI / PULSE_TICKS);
        float pulseDepth = Math.min(1.0f, gone * 2.5f);
        return (1.0f - gone) * (1.0f - pulseDepth * (1.0f - wave));
    }
}

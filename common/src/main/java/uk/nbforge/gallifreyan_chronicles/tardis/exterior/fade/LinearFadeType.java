package uk.nbforge.gallifreyan_chronicles.tardis.exterior.fade;

import com.mojang.serialization.MapCodec;
import uk.nbforge.gallifreyan_chronicles.tardis.codec.exterior.fade.FadeCodec;

public final class LinearFadeType extends FadeType {
    private final float curve;

    public LinearFadeType(String id, FadeTiming timing, float curve) {
        super(id, timing);
        this.curve = curve;
    }

    public float curve() {
        return curve;
    }

    @Override
    public MapCodec<LinearFadeType> codec() {
        return FadeCodec.LINEAR;
    }

    @Override
    public float opacity(float gone, float elapsed) {
        float solid = 1.0f - gone;
        return curve == 1.0f ? solid : (float) Math.pow(solid, curve);
    }

    @Override
    public LinearFadeType withId(String id) {
        return new LinearFadeType(id, timing(), curve);
    }
}

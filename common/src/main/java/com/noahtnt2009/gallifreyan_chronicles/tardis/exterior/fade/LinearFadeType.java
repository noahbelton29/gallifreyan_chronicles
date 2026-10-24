package com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.fade;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public final class LinearFadeType extends FadeType {
    public static final MapCodec<LinearFadeType> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            FadeTiming.CODEC.forGetter(LinearFadeType::timing),
            Codec.floatRange(0.1f, 10.0f).optionalFieldOf("curve", 1.0f)
                    .forGetter(LinearFadeType::curve)
    ).apply(inst, (timing, curve) -> new LinearFadeType("", timing, curve)));

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
        return CODEC;
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

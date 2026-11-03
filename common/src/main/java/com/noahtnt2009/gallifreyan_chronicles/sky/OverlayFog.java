package com.noahtnt2009.gallifreyan_chronicles.sky;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record OverlayFog(int color, float start, float end) {
    public static final Codec<OverlayFog> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            HexColor.CODEC.optionalFieldOf("color", 0xFF000000).forGetter(OverlayFog::color),
            Codec.floatRange(0.0F, 1.0F).optionalFieldOf("start", 0.3F).forGetter(OverlayFog::start),
            Codec.floatRange(0.05F, 1.5F).optionalFieldOf("end", 1.0F).forGetter(OverlayFog::end)
    ).apply(inst, OverlayFog::new));
}

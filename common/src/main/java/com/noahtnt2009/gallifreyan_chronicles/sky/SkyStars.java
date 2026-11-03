package com.noahtnt2009.gallifreyan_chronicles.sky;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record SkyStars(int count, long seed, float minSize, float maxSize, float brightness, int color,
                       boolean fadesInRain, float twinkle, float twinkleSpeed) {
    public static final Codec<SkyStars> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.intRange(0, 20000).optionalFieldOf("count", 1500).forGetter(SkyStars::count),
            Codec.LONG.optionalFieldOf("seed", 10842L).forGetter(SkyStars::seed),
            Codec.floatRange(0.01F, 5.0F).optionalFieldOf("min_size", 0.15F).forGetter(SkyStars::minSize),
            Codec.floatRange(0.01F, 5.0F).optionalFieldOf("max_size", 0.25F).forGetter(SkyStars::maxSize),
            Codec.floatRange(0.0F, 1.0F).optionalFieldOf("brightness", 1.0F).forGetter(SkyStars::brightness),
            HexColor.CODEC.optionalFieldOf("color", 0xFFFFFFFF).forGetter(SkyStars::color),
            Codec.BOOL.optionalFieldOf("fades_in_rain", true).forGetter(SkyStars::fadesInRain),
            Codec.floatRange(0.0F, 1.0F).optionalFieldOf("twinkle", 0.0F).forGetter(SkyStars::twinkle),
            Codec.floatRange(0.0F, 10.0F).optionalFieldOf("twinkle_speed", 1.0F).forGetter(SkyStars::twinkleSpeed)
    ).apply(inst, SkyStars::new));
}

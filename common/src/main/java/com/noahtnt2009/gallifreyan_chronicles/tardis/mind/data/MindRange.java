package com.noahtnt2009.gallifreyan_chronicles.tardis.mind.data;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

/* a min/max pair from json, picked from at random */
public record MindRange(float min, float max) {
    private static final Codec<MindRange> MIN_MAX = RecordCodecBuilder.create(inst -> inst.group(
            Codec.FLOAT.fieldOf("min").forGetter(MindRange::min),
            Codec.FLOAT.fieldOf("max").forGetter(MindRange::max)
    ).apply(inst, MindRange::new));

    public static final Codec<MindRange> CODEC = Codec.either(Codec.FLOAT, MIN_MAX).xmap(
            either -> either.map(MindRange::of, range -> range),
            range -> range.min == range.max ? Either.left(range.min) : Either.right(range));

    public static MindRange of(float value) {
        return new MindRange(value, value);
    }

    public float pick(RandomSource random) {
        return max <= min ? min : Mth.randomBetween(random, min, max);
    }

    public int pickInt(RandomSource random) {
        int low = Math.round(min);
        int high = Math.max(low, Math.round(max));
        return low + random.nextInt(high - low + 1);
    }
}

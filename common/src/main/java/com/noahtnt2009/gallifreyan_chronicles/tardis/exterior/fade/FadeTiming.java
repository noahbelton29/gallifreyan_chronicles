package com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.fade;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record FadeTiming(int takeoffDelayTicks, int fadeOutTicks, int fadeInTicks) {
    public static final FadeTiming DEFAULT = new FadeTiming(50, 210, 260);

    private static final int MAX_TICKS = 24000;

    public static final MapCodec<FadeTiming> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.intRange(0, MAX_TICKS).optionalFieldOf("takeoff_delay_ticks", DEFAULT.takeoffDelayTicks())
                    .forGetter(FadeTiming::takeoffDelayTicks),
            Codec.intRange(1, MAX_TICKS).optionalFieldOf("fade_out_ticks", DEFAULT.fadeOutTicks())
                    .forGetter(FadeTiming::fadeOutTicks),
            Codec.intRange(1, MAX_TICKS).optionalFieldOf("fade_in_ticks", DEFAULT.fadeInTicks())
                    .forGetter(FadeTiming::fadeInTicks)
    ).apply(inst, FadeTiming::new));
}

package uk.nbforge.gallifreyan_chronicles.tardis.codec.exterior.fade;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import uk.nbforge.gallifreyan_chronicles.tardis.exterior.fade.FadeTiming;
import uk.nbforge.gallifreyan_chronicles.tardis.exterior.fade.LinearFadeType;
import uk.nbforge.gallifreyan_chronicles.tardis.exterior.fade.PulseFadeType;

public final class FadeCodec {
    public static final MapCodec<FadeTiming> TIMING = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.intRange(0, FadeTiming.MAX_TICKS).optionalFieldOf("takeoff_delay_ticks", FadeTiming.DEFAULT.takeoffDelayTicks())
                    .forGetter(FadeTiming::takeoffDelayTicks),
            Codec.intRange(1, FadeTiming.MAX_TICKS).optionalFieldOf("fade_out_ticks", FadeTiming.DEFAULT.fadeOutTicks())
                    .forGetter(FadeTiming::fadeOutTicks),
            Codec.intRange(1, FadeTiming.MAX_TICKS).optionalFieldOf("fade_in_ticks", FadeTiming.DEFAULT.fadeInTicks())
                    .forGetter(FadeTiming::fadeInTicks)
    ).apply(inst, FadeTiming::new));

    public static final MapCodec<LinearFadeType> LINEAR = RecordCodecBuilder.mapCodec(inst -> inst.group(
            TIMING.forGetter(LinearFadeType::timing),
            Codec.floatRange(0.1f, 10.0f).optionalFieldOf("curve", 1.0f)
                    .forGetter(LinearFadeType::curve)
    ).apply(inst, (timing, curve) -> new LinearFadeType("", timing, curve)));

    public static final MapCodec<PulseFadeType> PULSE = RecordCodecBuilder.mapCodec(inst -> inst.group(
            TIMING.forGetter(PulseFadeType::timing),
            Codec.floatRange(1.0f, 1200.0f).optionalFieldOf("pulse_ticks", 35.0f)
                    .forGetter(PulseFadeType::pulseTicks),
            Codec.floatRange(0.0f, 100.0f).optionalFieldOf("pulse_ramp", 2.5f)
                    .forGetter(PulseFadeType::pulseRamp),
            Codec.floatRange(0.0f, 1.0f).optionalFieldOf("pulse_depth", 1.0f)
                    .forGetter(PulseFadeType::pulseDepth)
    ).apply(inst, (timing, pulseTicks, pulseRamp, pulseDepth) ->
            new PulseFadeType("", timing, pulseTicks, pulseRamp, pulseDepth)));

    private FadeCodec() {
    }
}

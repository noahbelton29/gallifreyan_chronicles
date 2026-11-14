package uk.nbforge.gallifreyan_chronicles.tardis.codec.mind;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.data.TraitModifiers;

public final class TraitModifiersCodec {
    private record Stress(float buildsUp, float calmsDown) {
        static final Stress SAME = new Stress(1f, 1f);
        static final Codec<Stress> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Codec.FLOAT.optionalFieldOf("builds_up", 1f).forGetter(Stress::buildsUp),
                Codec.FLOAT.optionalFieldOf("calms_down", 1f).forGetter(Stress::calmsDown)
        ).apply(inst, Stress::new));
    }

    private record Fear(float fades) {
        static final Fear SAME = new Fear(1f);
        static final Codec<Fear> CODEC = Codec.FLOAT.optionalFieldOf("fades", 1f).codec().xmap(Fear::new, Fear::fades);
    }

    private record Trust(float gained, float lost, float lostWhenAbused, Optional<Float> startsAt,
                         Optional<Float> strangersStartAt) {
        static final Trust SAME = new Trust(1f, 1f, 1f, Optional.empty(), Optional.empty());
        static final Codec<Trust> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Codec.FLOAT.optionalFieldOf("gained", 1f).forGetter(Trust::gained),
                Codec.FLOAT.optionalFieldOf("lost", 1f).forGetter(Trust::lost),
                Codec.FLOAT.optionalFieldOf("lost_when_abused", 1f).forGetter(Trust::lostWhenAbused),
                Codec.FLOAT.optionalFieldOf("starts_at").forGetter(Trust::startsAt),
                Codec.FLOAT.optionalFieldOf("strangers_start_at").forGetter(Trust::strangersStartAt)
        ).apply(inst, Trust::new));
    }

    private record Betrayal(float trustGained, float trustLost, float traumaTaken) {
        static final Codec<Betrayal> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Codec.FLOAT.optionalFieldOf("trust_gained", 1f).forGetter(Betrayal::trustGained),
                Codec.FLOAT.optionalFieldOf("trust_lost", 1f).forGetter(Betrayal::trustLost),
                Codec.FLOAT.optionalFieldOf("trauma_taken", 1f).forGetter(Betrayal::traumaTaken)
        ).apply(inst, Betrayal::new));
    }

    public static final MapCodec<TraitModifiers> MAP_CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Stress.CODEC.optionalFieldOf("stress", Stress.SAME).forGetter(m -> new Stress(m.stressGain(), m.stressFade())),
            Fear.CODEC.optionalFieldOf("fear", Fear.SAME).forGetter(m -> new Fear(m.fearFade())),
            Trust.CODEC.optionalFieldOf("trust", Trust.SAME)
                    .forGetter(m -> new Trust(m.trustGain(), m.trustLoss(), m.abuseTrustLoss(), m.startTrust(), m.strangerStartTrust())),
            Codec.FLOAT.optionalFieldOf("refuses_sooner_by", 0f).forGetter(TraitModifiers::refusalShift),
            Codec.FLOAT.optionalFieldOf("refuses_strangers_sooner_by", 0f).forGetter(TraitModifiers::strangerShift),
            Codec.FLOAT.optionalFieldOf("crash_damage", 1f).forGetter(TraitModifiers::crashSoftening),
            Codec.FLOAT.optionalFieldOf("trauma_taken", 1f).forGetter(TraitModifiers::traumaGain),
            Betrayal.CODEC.optionalFieldOf("if_betrayed").forGetter(m -> m.betrayable()
                    ? Optional.of(new Betrayal(m.betrayedTrustGain(), m.betrayedTrustLoss(), m.betrayedTraumaGain()))
                    : Optional.empty())
    ).apply(inst, (stress, fear, trust, sooner, strangers, crash, trauma, betrayal) -> new TraitModifiers(
            stress.buildsUp(), stress.calmsDown(), fear.fades(), trust.gained(), trust.lost(), sooner, strangers,
            crash, trust.startsAt(), trust.strangersStartAt(), trust.lostWhenAbused(), trauma, betrayal.isPresent(),
            betrayal.map(Betrayal::trustGained).orElse(1f), betrayal.map(Betrayal::trustLost).orElse(1f),
            betrayal.map(Betrayal::traumaTaken).orElse(1f))));

    private TraitModifiersCodec() {
    }
}

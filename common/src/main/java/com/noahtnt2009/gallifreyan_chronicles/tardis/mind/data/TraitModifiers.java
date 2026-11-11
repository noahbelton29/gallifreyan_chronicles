package com.noahtnt2009.gallifreyan_chronicles.tardis.mind.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.TardisTrait;

import java.util.List;
import java.util.Optional;

/* the numbers a trait changes. multipliers default to 1, shifts to 0 */
public record TraitModifiers(float stressGain, float stressFade, float fearFade, float trustGain, float trustLoss,
                             float refusalShift, float strangerShift, float crashSoftening, Optional<Float> startTrust,
                             float abuseTrustLoss, float traumaGain, boolean betrayable,
                             float betrayedTrustGain, float betrayedTrustLoss, float betrayedTraumaGain) {
    public static final TraitModifiers NONE = new TraitModifiers(1f, 1f, 1f, 1f, 1f, 0f, 0f, 1f, Optional.empty(),
            1f, 1f, false, 1f, 1f, 1f);

    public static final Codec<TraitModifiers> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.FLOAT.optionalFieldOf("stress_gain", 1f).forGetter(TraitModifiers::stressGain),
            Codec.FLOAT.optionalFieldOf("stress_fade", 1f).forGetter(TraitModifiers::stressFade),
            Codec.FLOAT.optionalFieldOf("fear_fade", 1f).forGetter(TraitModifiers::fearFade),
            Codec.FLOAT.optionalFieldOf("trust_gain", 1f).forGetter(TraitModifiers::trustGain),
            Codec.FLOAT.optionalFieldOf("trust_loss", 1f).forGetter(TraitModifiers::trustLoss),
            Codec.FLOAT.optionalFieldOf("refusal_shift", 0f).forGetter(TraitModifiers::refusalShift),
            Codec.FLOAT.optionalFieldOf("stranger_shift", 0f).forGetter(TraitModifiers::strangerShift),
            Codec.FLOAT.optionalFieldOf("crash_softening", 1f).forGetter(TraitModifiers::crashSoftening),
            Codec.FLOAT.optionalFieldOf("start_trust").forGetter(TraitModifiers::startTrust),
            Codec.FLOAT.optionalFieldOf("abuse_trust_loss", 1f).forGetter(TraitModifiers::abuseTrustLoss),
            Codec.FLOAT.optionalFieldOf("trauma_gain", 1f).forGetter(TraitModifiers::traumaGain),
            Codec.BOOL.optionalFieldOf("betrayable", false).forGetter(TraitModifiers::betrayable),
            Codec.FLOAT.optionalFieldOf("betrayed_trust_gain", 1f).forGetter(TraitModifiers::betrayedTrustGain),
            Codec.FLOAT.optionalFieldOf("betrayed_trust_loss", 1f).forGetter(TraitModifiers::betrayedTrustLoss),
            Codec.FLOAT.optionalFieldOf("betrayed_trauma_gain", 1f).forGetter(TraitModifiers::betrayedTraumaGain)
    ).apply(inst, TraitModifiers::new));

    /* everything a tardis's traits add up to */
    public static TraitModifiers combine(List<TardisTrait> traits, boolean betrayed) {
        float stressGain = 1f, stressFade = 1f, fearFade = 1f, trustGain = 1f, trustLoss = 1f;
        float refusalShift = 0f, strangerShift = 0f, crashSoftening = 1f, abuse = 1f, trauma = 1f;
        Optional<Float> startTrust = Optional.empty();
        boolean betrayable = false;

        for (TardisTrait trait : traits) {
            TraitModifiers m = trait.modifiers();
            boolean hurt = betrayed && m.betrayable();
            stressGain *= m.stressGain;
            stressFade *= m.stressFade;
            fearFade *= m.fearFade;
            trustGain *= hurt ? m.betrayedTrustGain : m.trustGain;
            trustLoss *= hurt ? m.betrayedTrustLoss : m.trustLoss;
            refusalShift += m.refusalShift;
            strangerShift += m.strangerShift;
            crashSoftening *= m.crashSoftening;
            abuse *= m.abuseTrustLoss;
            trauma *= hurt ? m.betrayedTraumaGain : m.traumaGain;
            betrayable |= m.betrayable;
            if (m.startTrust.isPresent() && (startTrust.isEmpty() || m.startTrust.get() < startTrust.get())) {
                startTrust = m.startTrust;
            }
        }
        return new TraitModifiers(stressGain, stressFade, fearFade, trustGain, trustLoss, refusalShift, strangerShift,
                crashSoftening, startTrust, abuse, trauma, betrayable, 1f, 1f, 1f);
    }
}

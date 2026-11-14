package uk.nbforge.gallifreyan_chronicles.tardis.mind.data;

import uk.nbforge.gallifreyan_chronicles.tardis.mind.TardisTrait;

import java.util.List;
import java.util.Optional;

/* the numbers a trait changes. "times" values default to 1, "by" values to 0 */
public record TraitModifiers(float stressGain, float stressFade, float fearFade, float trustGain, float trustLoss,
                             float refusalShift, float strangerShift, float crashSoftening, Optional<Float> startTrust,
                             Optional<Float> strangerStartTrust, float abuseTrustLoss, float traumaGain, boolean betrayable,
                             float betrayedTrustGain, float betrayedTrustLoss, float betrayedTraumaGain) {
    public static final TraitModifiers NONE = new TraitModifiers(1f, 1f, 1f, 1f, 1f, 0f, 0f, 1f, Optional.empty(),
            Optional.empty(), 1f, 1f, false, 1f, 1f, 1f);

    /* everything a tardis's traits add up to */
    public static TraitModifiers combine(List<TardisTrait> traits, boolean betrayed) {
        float stressGain = 1f, stressFade = 1f, fearFade = 1f, trustGain = 1f, trustLoss = 1f;
        float refusalShift = 0f, strangerShift = 0f, crashSoftening = 1f, abuse = 1f, trauma = 1f;
        Optional<Float> startTrust = Optional.empty();
        Optional<Float> strangerStartTrust = Optional.empty();
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
            startTrust = lowest(startTrust, m.startTrust);
            strangerStartTrust = lowest(strangerStartTrust, m.strangerStartTrust);
        }
        return new TraitModifiers(stressGain, stressFade, fearFade, trustGain, trustLoss, refusalShift, strangerShift,
                crashSoftening, startTrust, strangerStartTrust, abuse, trauma, betrayable, 1f, 1f, 1f);
    }

    private static Optional<Float> lowest(Optional<Float> a, Optional<Float> b) {
        if (a.isEmpty()) return b;
        if (b.isEmpty()) return a;
        return Optional.of(Math.min(a.get(), b.get()));
    }
}

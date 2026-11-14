package uk.nbforge.gallifreyan_chronicles.datagen.tardis;

import com.google.gson.JsonObject;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.data.MindEnums.Reveal;
import net.minecraft.data.PackOutput;
import uk.nbforge.gallifreyan_chronicles.datagen.util.GCJsonDataProvider;
import uk.nbforge.gallifreyan_chronicles.datagen.tardis.builder.GCTraitBuilder;

/**
 * Generates data/gallifreyan_chronicles/tardis_trait/*.json.
 * To add one, copy a block below and change what you want. Multipliers ("times") default to 1 and shifts to 0,
 * so only list what the trait changes. Reactions to events come from {@link GCMind}.
 */
public class GCTardisTraitProvider extends GCJsonDataProvider {
    public GCTardisTraitProvider(PackOutput output) {
        super(output, "tardis_trait", "TARDIS Trait");
    }

    @Override
    protected void addEntries() {
        trait("distrusting")
                .howCommon(10)
                .trustGained(0.6)
                .trustLost(0.6)
                .trustLostWhenAbused(2.5)
                .startsWithTrust(12)
                .traumaTaken(0.7)
                .neverWithTraits("loyal", "welcoming", "forgiving")
                .neverWithPersonalities("obedient")
                .revealedWhen(Reveal.ITS_CONTROLS_STICK);

        trait("forgiving")
                .howCommon(10)
                .stressBuildsUp(1.15)
                .stressCalmsDown(1.6)
                .fearFades(1.6)
                .neverWithTraits("grudge_holder")
                .neverWithPersonalities("aggressive")
                .revealedWhen(Reveal.IT_LANDS_UPSET);

        trait("grudge_holder")
                .howCommon(10)
                .stressCalmsDown(0.7)
                .fearFades(0.6)
                .trustGained(1.3)
                .trustLostWhenAbused(1.5)
                .neverWithPersonalities("obedient")
                .revealedWhen(Reveal.IT_LANDS_UPSET);

        trait("loyal")
                .howCommon(10)
                .trustGained(1.6)
                .trustLost(0.6)
                .traumaTaken(1.5)
                .ifBetrayed(0.5, 1.5, 2.0)
                .neverWithTraits("distrusting")
                .revealedWhen(Reveal.IT_LOSES_TRUST);

        trait("possessive")
                .howCommon(10)
                .trustGained(1.2)
                .strangersStartWithTrust(25)
                .refusesStrangersSoonerBy(15)
                .neverWithTraits("welcoming")
                .neverWithPersonalities("obedient")
                .revealedWhen(Reveal.A_STRANGER_UPSETS_IT);

        trait("protective")
                .howCommon(10)
                .stressBuildsUp(1.2)
                .crashDamage(0.6)
                .revealedWhen(Reveal.IT_CRASHES);

        trait("skittish")
                .howCommon(10)
                .stressBuildsUp(1.4)
                .stressCalmsDown(1.8)
                .trustGained(1.15)
                .revealedWhen(Reveal.IT_GETS_STRESSED);

        trait("stubborn")
                .howCommon(10)
                .trustLost(0.85)
                .traumaTaken(0.75)
                .refusesSoonerBy(8)
                .neverWithTraits("welcoming")
                .neverWithPersonalities("obedient", "submissive")
                .revealedWhen(Reveal.IT_REFUSES);

        trait("welcoming")
                .howCommon(10)
                .trustLost(1.25)
                .trustLostWhenAbused(1.3)
                .traumaTaken(1.15)
                .refusesStrangersSoonerBy(-10)
                .neverWithPersonalities("defensive", "aggressive")
                .revealedWhen(Reveal.A_STRANGER_UPSETS_IT);
    }

    private GCTraitBuilder trait(String name) {
        JsonObject json = new JsonObject();
        add(name, json);
        return new GCTraitBuilder(json);
    }
}

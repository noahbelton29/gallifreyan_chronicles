package uk.nbforge.gallifreyan_chronicles.tardis.codec.mind;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import uk.nbforge.gallifreyan_chronicles.tardis.codec.mind.data.MindRangeCodec;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.TardisPersonality;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.data.MindRange;
import uk.nbforge.gallifreyan_chronicles.tardis.codec.mind.action.MindActionCodec;

public final class TardisPersonalityCodec {
    private record RefusesWhen(float stressAbove, float trustBelow) {
        static final RefusesWhen NEVER = new RefusesWhen(101f, -1f);

        static final Codec<RefusesWhen> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Codec.FLOAT.optionalFieldOf("stress_above", 101f).forGetter(RefusesWhen::stressAbove),
                Codec.FLOAT.optionalFieldOf("trust_below", -1f).forGetter(RefusesWhen::trustBelow)
        ).apply(inst, RefusesWhen::new));
    }

    private record Mallet(MindRange fearPerHit, float fearNeededToObey, float traumaPerHit, MindRange terrorHits) {
        static final Mallet DEFAULT = new Mallet(MindRange.of(15f), 0f, 3f, new MindRange(1, 2));

        static final Codec<Mallet> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                MindRangeCodec.CODEC.optionalFieldOf("fear_per_hit", MindRange.of(15f)).forGetter(Mallet::fearPerHit),
                Codec.FLOAT.optionalFieldOf("fear_needed_to_obey", 0f).forGetter(Mallet::fearNeededToObey),
                Codec.FLOAT.optionalFieldOf("trauma_per_hit", 3f).forGetter(Mallet::traumaPerHit),
                MindRangeCodec.CODEC.optionalFieldOf("hits_to_snap_out_of_terror", new MindRange(1, 2)).forGetter(Mallet::terrorHits)
        ).apply(inst, Mallet::new));
    }

    /**
     * Codec for loading and saving TARDIS personalities.
     *
     * <p>Groups flight refusal thresholds and mallet behaviour into separate
     * configuration sections, while keeping their values in the main record.
     * Missing fields use their default values.
     */
    public static final Codec<TardisPersonality> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.STRING.optionalFieldOf("id", "").forGetter(TardisPersonality::id),
            Codec.INT.optionalFieldOf("how_common", 10).forGetter(TardisPersonality::weight),
            RefusesWhen.CODEC.optionalFieldOf("refuses_to_fly_when", RefusesWhen.NEVER)
                    .forGetter(p -> new RefusesWhen(p.stressLimit(), p.trustLimit())),
            Mallet.CODEC.optionalFieldOf("mallet", Mallet.DEFAULT)
                    .forGetter(p -> new Mallet(p.malletFear(), p.fearToComply(), p.traumaPerHit(), p.terrorHits())),
            MindActionCodec.LIST_CODEC.optionalFieldOf("when_unwilling_to_fly", List.of()).forGetter(TardisPersonality::onUnwilling),
            MindActionCodec.LIST_CODEC.optionalFieldOf("when_it_sees_the_mallet", List.of()).forGetter(TardisPersonality::onMalletSeen),
            MindActionCodec.LIST_CODEC.optionalFieldOf("when_hit", List.of()).forGetter(TardisPersonality::onMalletHit),
            MindActionCodec.LIST_CODEC.optionalFieldOf("when_hit_while_terrified", List.of()).forGetter(TardisPersonality::onTerrorHit),
            MindActionCodec.LIST_CODEC.optionalFieldOf("when_taking_off", List.of()).forGetter(TardisPersonality::onTakeOff),
            MindActionCodec.LIST_CODEC.optionalFieldOf("when_landing", List.of()).forGetter(TardisPersonality::onLand),
            MindActionCodec.LIST_CODEC.optionalFieldOf("when_you_enter", List.of()).forGetter(TardisPersonality::onEnter)
    ).apply(inst, (id, rarity, refuses, mallet, unwilling, seen, hit, terror, takeOff, land, enter) ->
            new TardisPersonality(id, rarity, refuses.stressAbove(), refuses.trustBelow(), mallet.fearNeededToObey(),
                    mallet.fearPerHit(), mallet.traumaPerHit(), mallet.terrorHits(),
                    unwilling, seen, hit, terror, takeOff, land, enter)));

    private TardisPersonalityCodec() {
    }
}

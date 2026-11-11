package com.noahtnt2009.gallifreyan_chronicles.tardis.mind;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.data.IdentifiableData;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.action.MindAction;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.action.MindActions;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.data.MindRange;
import com.noahtnt2009.gallifreyan_chronicles.util.GCUtils;

import java.util.List;

public record TardisPersonality(String id, int weight, float stressLimit, float trustLimit, float fearToComply,
                                MindRange malletFear, float traumaPerHit, MindRange terrorHits,
                                List<MindAction> onUnwilling, List<MindAction> onMalletSeen,
                                List<MindAction> onMalletHit, List<MindAction> onTerrorHit,
                                List<MindAction> onTakeOff, List<MindAction> onLand)
        implements IdentifiableData<TardisPersonality> {

    public static final TardisPersonality FALLBACK = new TardisPersonality(Constants.MOD_ID + ":obedient", 1,
            101f, -1f, 0f, MindRange.of(15f), 3f, new MindRange(1, 2),
            List.of(), List.of(), List.of(), List.of(), List.of(), List.of());

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
                MindRange.CODEC.optionalFieldOf("fear_per_hit", MindRange.of(15f)).forGetter(Mallet::fearPerHit),
                Codec.FLOAT.optionalFieldOf("fear_needed_to_obey", 0f).forGetter(Mallet::fearNeededToObey),
                Codec.FLOAT.optionalFieldOf("trauma_per_hit", 3f).forGetter(Mallet::traumaPerHit),
                MindRange.CODEC.optionalFieldOf("hits_to_snap_out_of_terror", new MindRange(1, 2)).forGetter(Mallet::terrorHits)
        ).apply(inst, Mallet::new));
    }

    public static final Codec<TardisPersonality> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.STRING.optionalFieldOf("id", "").forGetter(TardisPersonality::id),
            Codec.INT.optionalFieldOf("how_common", 10).forGetter(TardisPersonality::weight),
            RefusesWhen.CODEC.optionalFieldOf("refuses_to_fly_when", RefusesWhen.NEVER)
                    .forGetter(p -> new RefusesWhen(p.stressLimit, p.trustLimit)),
            Mallet.CODEC.optionalFieldOf("mallet", Mallet.DEFAULT)
                    .forGetter(p -> new Mallet(p.malletFear, p.fearToComply, p.traumaPerHit, p.terrorHits)),
            MindActions.LIST_CODEC.optionalFieldOf("when_unwilling_to_fly", List.of()).forGetter(TardisPersonality::onUnwilling),
            MindActions.LIST_CODEC.optionalFieldOf("when_it_sees_the_mallet", List.of()).forGetter(TardisPersonality::onMalletSeen),
            MindActions.LIST_CODEC.optionalFieldOf("when_hit", List.of()).forGetter(TardisPersonality::onMalletHit),
            MindActions.LIST_CODEC.optionalFieldOf("when_hit_while_terrified", List.of()).forGetter(TardisPersonality::onTerrorHit),
            MindActions.LIST_CODEC.optionalFieldOf("when_taking_off", List.of()).forGetter(TardisPersonality::onTakeOff),
            MindActions.LIST_CODEC.optionalFieldOf("when_landing", List.of()).forGetter(TardisPersonality::onLand)
    ).apply(inst, (id, rarity, refuses, mallet, unwilling, seen, hit, terror, takeOff, land) ->
            new TardisPersonality(id, rarity, refuses.stressAbove(), refuses.trustBelow(), mallet.fearNeededToObey(),
                    mallet.fearPerHit(), mallet.traumaPerHit(), mallet.terrorHits(),
                    unwilling, seen, hit, terror, takeOff, land)));

    public String localId() {
        return GCUtils.localPath(id);
    }

    /* nothing to do when unwilling means it always flies, like obedient */
    public boolean alwaysFlies() {
        return onUnwilling.isEmpty();
    }

    @Override
    public TardisPersonality withId(String id) {
        return new TardisPersonality(id, weight, stressLimit, trustLimit, fearToComply, malletFear, traumaPerHit,
                terrorHits, onUnwilling, onMalletSeen, onMalletHit, onTerrorHit, onTakeOff, onLand);
    }
}

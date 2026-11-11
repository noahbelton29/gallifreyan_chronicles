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

    public static final Codec<TardisPersonality> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.STRING.optionalFieldOf("id", "").forGetter(TardisPersonality::id),
            Codec.INT.optionalFieldOf("weight", 10).forGetter(TardisPersonality::weight),
            Codec.FLOAT.optionalFieldOf("stress_limit", 101f).forGetter(TardisPersonality::stressLimit),
            Codec.FLOAT.optionalFieldOf("trust_limit", -1f).forGetter(TardisPersonality::trustLimit),
            Codec.FLOAT.optionalFieldOf("fear_to_comply", 0f).forGetter(TardisPersonality::fearToComply),
            MindRange.CODEC.optionalFieldOf("mallet_fear", MindRange.of(15f)).forGetter(TardisPersonality::malletFear),
            Codec.FLOAT.optionalFieldOf("trauma_per_hit", 3f).forGetter(TardisPersonality::traumaPerHit),
            MindRange.CODEC.optionalFieldOf("terror_hits", new MindRange(1, 2)).forGetter(TardisPersonality::terrorHits),
            MindActions.LIST_CODEC.optionalFieldOf("on_unwilling", List.of()).forGetter(TardisPersonality::onUnwilling),
            MindActions.LIST_CODEC.optionalFieldOf("on_mallet_seen", List.of()).forGetter(TardisPersonality::onMalletSeen),
            MindActions.LIST_CODEC.optionalFieldOf("on_mallet_hit", List.of()).forGetter(TardisPersonality::onMalletHit),
            MindActions.LIST_CODEC.optionalFieldOf("on_terror_hit", List.of()).forGetter(TardisPersonality::onTerrorHit),
            MindActions.LIST_CODEC.optionalFieldOf("on_take_off", List.of()).forGetter(TardisPersonality::onTakeOff),
            MindActions.LIST_CODEC.optionalFieldOf("on_land", List.of()).forGetter(TardisPersonality::onLand)
    ).apply(inst, TardisPersonality::new));

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

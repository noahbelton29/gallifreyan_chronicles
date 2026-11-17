package uk.nbforge.gallifreyan_chronicles.tardis.mind;

import java.util.List;
import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.core.data.IdentifiableData;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.action.MindAction;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.data.MindRange;
import uk.nbforge.gallifreyan_chronicles.util.GCUtil;

/**
 * A personality of a TARDIS and how it responds to different situations.
 * It controls flight refusal, fear, trauma and the actions it does for events.
 * Personalities are loaded from data files using CODEC.
 */
public record TardisPersonality(
        String id,
        int weight,
        float stressLimit,
        float trustLimit,
        float fearToComply,
        MindRange malletFear,
        float traumaPerHit,
        MindRange terrorHits,
        List<MindAction> onUnwilling,
        List<MindAction> onMalletSeen,
        List<MindAction> onMalletHit,
        List<MindAction> onTerrorHit,
        List<MindAction> onTakeOff,
        List<MindAction> onLand,
        List<MindAction> onEnter)
        implements IdentifiableData<TardisPersonality> {

    // Used when no other personality is available
    public static final TardisPersonality FALLBACK = new TardisPersonality(
            Constants.MOD_ID + ":obedient",
            1,
            101f,
            -1f,
            0f,
            MindRange.of(15f),
            3f,
            new MindRange(1, 2),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of());

    public String localId() {
        return GCUtil.localPath(id);
    }

    /** It can refuse takeoff if it has actions for when it is unwilling. */
    public boolean refusesToFly() {
        return !onUnwilling.isEmpty();
    }

    @Override
    public TardisPersonality withId(String id) {
        return new TardisPersonality(
                id,
                weight,
                stressLimit,
                trustLimit,
                fearToComply,
                malletFear,
                traumaPerHit,
                terrorHits,
                onUnwilling,
                onMalletSeen,
                onMalletHit,
                onTerrorHit,
                onTakeOff,
                onLand,
                onEnter);
    }
}

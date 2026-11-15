package uk.nbforge.gallifreyan_chronicles.tardis.mind;

import uk.nbforge.gallifreyan_chronicles.core.data.IdentifiableData;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.action.MindAction;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.data.MindEnums.Reveal;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.data.MindIds;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.data.TraitModifiers;
import uk.nbforge.gallifreyan_chronicles.util.GCUtil;

import java.util.List;

/**
   A trait is a quirk that changes how a TARDIS acts.
   It can also block some personalities and some other traits.
*/
public record TardisTrait(
        String id,
        int weight,
        TraitModifiers modifiers,
        List<String> clashesWith,
        List<String> bannedPersonalities,
        Reveal reveal,
        List<MindAction> onUnwilling,
        List<MindAction> onTakeOff,
        List<MindAction> onLand
) implements IdentifiableData<TardisTrait> {

    /** The id without the mod name at the front. */
    public String localId() {
        return GCUtil.localPath(id);
    }

    /** Either trait can list the clash, so it only needs to be written once. */
    public boolean clashesWith(TardisTrait other) {
        return lists(clashesWith, other.id) || lists(other.clashesWith, id);
    }

    /** Not allowed if the personality is on the banned list. */
    public boolean fits(TardisPersonality personality) {
        return !lists(bannedPersonalities, personality.id());
    }

    /** Same as above but it also cannot clash with traits already picked. */
    public boolean fits(TardisPersonality personality, List<TardisTrait> others) {
        return fits(personality) && others.stream().noneMatch(this::clashesWith);
    }

    /** An id with no mod name counts as one from this mod. */
    private static boolean lists(List<String> ids, String id) {
        String wanted = MindIds.normalize(id);
        return ids.stream().anyMatch(each -> MindIds.normalize(each).equals(wanted));
    }

    @Override
    public TardisTrait withId(String id) {
        return new TardisTrait(id, weight, modifiers, clashesWith, bannedPersonalities, reveal, onUnwilling, onTakeOff, onLand);
    }
}
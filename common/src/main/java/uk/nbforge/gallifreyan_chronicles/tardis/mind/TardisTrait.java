package uk.nbforge.gallifreyan_chronicles.tardis.mind;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import uk.nbforge.gallifreyan_chronicles.loader.IdentifiableData;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.action.MindAction;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.data.MindEnums;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.data.MindEnums.Reveal;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.data.MindIds;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.data.TraitModifiers;
import uk.nbforge.gallifreyan_chronicles.util.GCUtil;

public record TardisTrait(
        String id,
        int weight,
        TraitModifiers modifiers,
        List<String> clashesWith,
        List<String> bannedPersonalities,
        Reveal reveal,
        List<MindAction> onUnwilling,
        List<MindAction> onTakeOff,
        List<MindAction> onLand)
        implements IdentifiableData<TardisTrait> {
    /** traits are the ones that clash with this trait, personalities are the ones that cant use it */
    private record NeverWith(List<String> traits, List<String> personalities) {

        static final NeverWith NOTHING = new NeverWith(List.of(), List.of());

        static final Codec<NeverWith> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                        Codec.STRING
                                .listOf()
                                .optionalFieldOf("traits", List.of())
                                .forGetter(NeverWith::traits),
                        Codec.STRING
                                .listOf()
                                .optionalFieldOf("personalities", List.of())
                                .forGetter(NeverWith::personalities))
                .apply(inst, NeverWith::new));
    }

    public static final Codec<TardisTrait> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                    Codec.STRING.optionalFieldOf("id", "").forGetter(TardisTrait::id),
                    Codec.INT.optionalFieldOf("how_common", 10).forGetter(TardisTrait::weight),
                    TraitModifiers.MAP_CODEC.forGetter(TardisTrait::modifiers),
                    NeverWith.CODEC
                            .optionalFieldOf("never_with", NeverWith.NOTHING)
                            .forGetter(t -> new NeverWith(t.clashesWith(), t.bannedPersonalities())),
                    MindEnums.codec(Reveal.class)
                            .optionalFieldOf("revealed_when", Reveal.NEVER)
                            .forGetter(TardisTrait::reveal),
                    MindAction.LIST_CODEC
                            .optionalFieldOf("when_unwilling_to_fly", List.of())
                            .forGetter(TardisTrait::onUnwilling),
                    MindAction.LIST_CODEC
                            .optionalFieldOf("when_taking_off", List.of())
                            .forGetter(TardisTrait::onTakeOff),
                    MindAction.LIST_CODEC
                            .optionalFieldOf("when_landing", List.of())
                            .forGetter(TardisTrait::onLand))
            .apply(
                    inst,
                    (id, rarity, modifiers, never, reveal, unwilling, takeOff, land) -> new TardisTrait(
                            id,
                            rarity,
                            modifiers,
                            never.traits(),
                            never.personalities(),
                            reveal,
                            unwilling,
                            takeOff,
                            land)));

    public String localId() {
        return GCUtil.localPath(id);
    }

    /** either trait can list the clash so it only needs writing once */
    public boolean clashesWith(TardisTrait other) {
        return lists(clashesWith, other.id) || lists(other.clashesWith, id);
    }

    public boolean fits(TardisPersonality personality) {
        return !lists(bannedPersonalities, personality.id());
    }

    public boolean fits(TardisPersonality personality, List<TardisTrait> others) {
        return fits(personality) && others.stream().noneMatch(this::clashesWith);
    }

    /** an id with no namespace counts as the mod's */
    private static boolean lists(List<String> ids, String id) {
        String wanted = MindIds.normalize(id);
        return ids.stream().anyMatch(each -> MindIds.normalize(each).equals(wanted));
    }

    @Override
    public TardisTrait withId(String id) {
        return new TardisTrait(
                id, weight, modifiers, clashesWith, bannedPersonalities, reveal, onUnwilling, onTakeOff, onLand);
    }
}

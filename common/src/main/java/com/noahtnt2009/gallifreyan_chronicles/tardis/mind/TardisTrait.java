package com.noahtnt2009.gallifreyan_chronicles.tardis.mind;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.noahtnt2009.gallifreyan_chronicles.data.IdentifiableData;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.action.MindAction;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.action.MindActions;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.data.MindEnums;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.data.MindEnums.Reveal;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.data.MindIds;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.data.TraitModifiers;
import com.noahtnt2009.gallifreyan_chronicles.util.GCUtils;

import java.util.List;

/**
 * Defines a trait that affects a TARDIS's personality and behaviour.
 *
 * <p>Traits can modify how a TARDIS thinks and behaves, determine which
 * personalities they are compatible with, and provide actions for different
 * situations such as refusing to fly, taking off or landing.</p>
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

    /**
     * Stores the traits and personalities that cannot be used together
     * with another trait.
     *
     * @param traits        IDs of traits that conflict with this trait
     * @param personalities IDs of personalities that cannot use this trait
     */
    private record NeverWith(List<String> traits, List<String> personalities) {

        /**
         * Empty compatibility restrictions.
         */
        static final NeverWith NOTHING = new NeverWith(List.of(), List.of());

        /**
         * Codec for loading and saving compatibility restrictions.
         *
         * <p>Both lists are optional and default to empty when not provided.</p>
         */
        static final Codec<NeverWith> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Codec.STRING.listOf().optionalFieldOf("traits", List.of()).forGetter(NeverWith::traits),
                Codec.STRING.listOf().optionalFieldOf("personalities", List.of()).forGetter(NeverWith::personalities)
        ).apply(inst, NeverWith::new));
    }

    /**
     * Codec for loading and saving TARDIS traits.
     *
     * <p>Optional fields use defaults so older or incomplete trait definitions
     * can still be loaded.</p>
     *
     * <p>The {@code never_with} field groups together traits and personalities
     * that are incompatible with this trait.</p>
     */
    public static final Codec<TardisTrait> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.STRING.optionalFieldOf("id", "").forGetter(TardisTrait::id),
            Codec.INT.optionalFieldOf("how_common", 10).forGetter(TardisTrait::weight),
            TraitModifiers.MAP_CODEC.forGetter(TardisTrait::modifiers),
            NeverWith.CODEC.optionalFieldOf("never_with", NeverWith.NOTHING)
                    .forGetter(t -> new NeverWith(t.clashesWith, t.bannedPersonalities)),
            MindEnums.codec(Reveal.class).optionalFieldOf("revealed_when", Reveal.NEVER).forGetter(TardisTrait::reveal),
            MindActions.LIST_CODEC.optionalFieldOf("when_unwilling_to_fly", List.of()).forGetter(TardisTrait::onUnwilling),
            MindActions.LIST_CODEC.optionalFieldOf("when_taking_off", List.of()).forGetter(TardisTrait::onTakeOff),
            MindActions.LIST_CODEC.optionalFieldOf("when_landing", List.of()).forGetter(TardisTrait::onLand)
    ).apply(inst, (id, rarity, modifiers, never, reveal, unwilling, takeOff, land) ->
            new TardisTrait(id, rarity, modifiers, never.traits(), never.personalities(), reveal, unwilling, takeOff, land)));

    /**
     * Gets the part of this trait's ID after its namespace.
     *
     * <p>For example, {@code gallifreyan_chronicles:brave} becomes
     * {@code brave}.</p>
     *
     * @return the local part of this trait's ID
     */
    public String localId() {
        return GCUtils.localPath(id);
    }

    /**
     * Checks whether this trait conflicts with another trait.
     *
     * <p>A conflict exists if either trait lists the other as incompatible.
     * This means compatibility restrictions only need to be declared on
     * one of the two traits.</p>
     *
     * @param other the trait to check against
     * @return whether either trait conflicts with the other
     */
    public boolean clashesWith(TardisTrait other) {
        return lists(clashesWith, other.id) || lists(other.clashesWith, id);
    }

    /**
     * Checks whether this trait is compatible with a personality.
     *
     * <p>A personality is incompatible when its ID appears in this trait's
     * {@code bannedPersonalities} list.</p>
     *
     * @param personality the personality to check
     * @return whether this trait can be used with that personality
     */
    public boolean fits(TardisPersonality personality) {
        return !lists(bannedPersonalities, personality.id());
    }

    /**
     * Checks whether this trait is compatible with a personality and a list
     * of other traits.
     *
     * <p>The trait must be compatible with the personality and must not
     * conflict with any of the other traits.</p>
     *
     * @param personality the personality to check
     * @param others      the other traits being used
     * @return whether this trait can be used alongside the given personality and traits
     */
    public boolean fits(TardisPersonality personality, List<TardisTrait> others) {
        return fits(personality) && others.stream().noneMatch(this::clashesWith);
    }

    /**
     * Checks whether a list of IDs contains a matching ID.
     *
     * <p>Both the requested ID and the IDs in the list are normalised before
     * comparison, allowing equivalent IDs to match even if their formatting
     * differs.</p>
     *
     * @param ids the list of IDs to search
     * @param id  the ID to look for
     * @return whether the list contains the requested ID
     */
    private static boolean lists(List<String> ids, String id) {
        String wanted = MindIds.normalize(id);
        return ids.stream().anyMatch(each -> MindIds.normalize(each).equals(wanted));
    }

    /**
     * Creates a copy of this trait with a different ID.
     *
     * <p>All other trait properties remain unchanged.</p>
     *
     * @param id the new ID
     * @return a copy of this trait with the supplied ID
     */
    @Override
    public TardisTrait withId(String id) {
        return new TardisTrait(id, weight, modifiers, clashesWith, bannedPersonalities, reveal, onUnwilling, onTakeOff, onLand);
    }
}
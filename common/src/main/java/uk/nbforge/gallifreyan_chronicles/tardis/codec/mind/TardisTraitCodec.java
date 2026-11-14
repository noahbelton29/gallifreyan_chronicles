package uk.nbforge.gallifreyan_chronicles.tardis.codec.mind;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import uk.nbforge.gallifreyan_chronicles.tardis.codec.mind.TraitModifiersCodec;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.TardisTrait;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.data.MindEnums.Reveal;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.data.MindEnums;
import uk.nbforge.gallifreyan_chronicles.tardis.codec.mind.action.MindActionCodec;

public final class TardisTraitCodec {
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
            TraitModifiersCodec.MAP_CODEC.forGetter(TardisTrait::modifiers),
            NeverWith.CODEC.optionalFieldOf("never_with", NeverWith.NOTHING)
                    .forGetter(t -> new NeverWith(t.clashesWith(), t.bannedPersonalities())),
            MindEnums.codec(Reveal.class).optionalFieldOf("revealed_when", Reveal.NEVER).forGetter(TardisTrait::reveal),
            MindActionCodec.LIST_CODEC.optionalFieldOf("when_unwilling_to_fly", List.of()).forGetter(TardisTrait::onUnwilling),
            MindActionCodec.LIST_CODEC.optionalFieldOf("when_taking_off", List.of()).forGetter(TardisTrait::onTakeOff),
            MindActionCodec.LIST_CODEC.optionalFieldOf("when_landing", List.of()).forGetter(TardisTrait::onLand)
    ).apply(inst, (id, rarity, modifiers, never, reveal, unwilling, takeOff, land) ->
            new TardisTrait(id, rarity, modifiers, never.traits(), never.personalities(), reveal, unwilling, takeOff, land)));

    private TardisTraitCodec() {
    }
}

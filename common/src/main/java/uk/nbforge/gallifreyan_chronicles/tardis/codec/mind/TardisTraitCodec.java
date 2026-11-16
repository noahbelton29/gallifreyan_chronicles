package uk.nbforge.gallifreyan_chronicles.tardis.codec.mind;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import uk.nbforge.gallifreyan_chronicles.tardis.codec.mind.action.MindActionCodec;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.TardisTrait;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.data.MindEnums;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.data.MindEnums.Reveal;

public final class TardisTraitCodec {
    /**
     * The traits and personalities that cannot be used with a trait.
     * traits are the ones that clash with this trait and personalities are the ones that cannot use it.
     */
    private record NeverWith(List<String> traits, List<String> personalities) {

        static final NeverWith NOTHING = new NeverWith(List.of(), List.of());

        // Both lists are optional and empty by default
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

    // Optional fields have defaults so older or incomplete trait files still load
    // never_with holds the traits and personalities that are not allowed with this trait
    public static final Codec<TardisTrait> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                    Codec.STRING.optionalFieldOf("id", "").forGetter(TardisTrait::id),
                    Codec.INT.optionalFieldOf("how_common", 10).forGetter(TardisTrait::weight),
                    TraitModifiersCodec.MAP_CODEC.forGetter(TardisTrait::modifiers),
                    NeverWith.CODEC
                            .optionalFieldOf("never_with", NeverWith.NOTHING)
                            .forGetter(t -> new NeverWith(t.clashesWith(), t.bannedPersonalities())),
                    MindEnums.codec(Reveal.class)
                            .optionalFieldOf("revealed_when", Reveal.NEVER)
                            .forGetter(TardisTrait::reveal),
                    MindActionCodec.LIST_CODEC
                            .optionalFieldOf("when_unwilling_to_fly", List.of())
                            .forGetter(TardisTrait::onUnwilling),
                    MindActionCodec.LIST_CODEC
                            .optionalFieldOf("when_taking_off", List.of())
                            .forGetter(TardisTrait::onTakeOff),
                    MindActionCodec.LIST_CODEC
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

    private TardisTraitCodec() {}
}

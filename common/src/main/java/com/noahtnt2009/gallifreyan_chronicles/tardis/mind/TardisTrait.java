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

public record TardisTrait(String id, int weight, TraitModifiers modifiers, List<String> clashesWith,
                          List<String> bannedPersonalities, Reveal reveal,
                          List<MindAction> onUnwilling, List<MindAction> onTakeOff, List<MindAction> onLand)
        implements IdentifiableData<TardisTrait> {

    public static final Codec<TardisTrait> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.STRING.optionalFieldOf("id", "").forGetter(TardisTrait::id),
            Codec.INT.optionalFieldOf("weight", 10).forGetter(TardisTrait::weight),
            TraitModifiers.CODEC.optionalFieldOf("modifiers", TraitModifiers.NONE).forGetter(TardisTrait::modifiers),
            Codec.STRING.listOf().optionalFieldOf("clashes_with", List.of()).forGetter(TardisTrait::clashesWith),
            Codec.STRING.listOf().optionalFieldOf("banned_personalities", List.of()).forGetter(TardisTrait::bannedPersonalities),
            MindEnums.codec(Reveal.class).optionalFieldOf("reveal", Reveal.NEVER).forGetter(TardisTrait::reveal),
            MindActions.LIST_CODEC.optionalFieldOf("on_unwilling", List.of()).forGetter(TardisTrait::onUnwilling),
            MindActions.LIST_CODEC.optionalFieldOf("on_take_off", List.of()).forGetter(TardisTrait::onTakeOff),
            MindActions.LIST_CODEC.optionalFieldOf("on_land", List.of()).forGetter(TardisTrait::onLand)
    ).apply(inst, TardisTrait::new));

    public String localId() {
        return GCUtils.localPath(id);
    }

    public boolean clashesWith(TardisTrait other) {
        return lists(clashesWith, other.id) || lists(other.clashesWith, id);
    }

    public boolean fits(TardisPersonality personality) {
        return !lists(bannedPersonalities, personality.id());
    }

    public boolean fits(TardisPersonality personality, List<TardisTrait> others) {
        return fits(personality) && others.stream().noneMatch(this::clashesWith);
    }

    private static boolean lists(List<String> ids, String id) {
        String wanted = MindIds.normalize(id);
        return ids.stream().anyMatch(each -> MindIds.normalize(each).equals(wanted));
    }

    @Override
    public TardisTrait withId(String id) {
        return new TardisTrait(id, weight, modifiers, clashesWith, bannedPersonalities, reveal, onUnwilling, onTakeOff, onLand);
    }
}

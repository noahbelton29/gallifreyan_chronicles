package com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.noahtnt2009.gallifreyan_chronicles.ecs.Component;

public record DoorVariantComponent(String variantId) implements Component {
    public static final Codec<DoorVariantComponent> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.STRING.optionalFieldOf("variant_id", "").forGetter(DoorVariantComponent::variantId)
    ).apply(inst, DoorVariantComponent::new));

    public static DoorVariantComponent unset() {
        return new DoorVariantComponent("");
    }

    public boolean isSet() {
        return !variantId.isEmpty();
    }
}

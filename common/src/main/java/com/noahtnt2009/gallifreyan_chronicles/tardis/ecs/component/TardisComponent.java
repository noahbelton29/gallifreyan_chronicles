package com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.UUID;

public record TardisComponent(UUID tardisId, UUID ownerId) {
    public static final Codec<UUID> UUID_CODEC = Codec.STRING.xmap(UUID::fromString, UUID::toString);

    public static final Codec<TardisComponent> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            UUID_CODEC.fieldOf("tardis_id").forGetter(TardisComponent::tardisId),
            UUID_CODEC.fieldOf("owner_id").forGetter(TardisComponent::ownerId)
    ).apply(inst, TardisComponent::new));
}

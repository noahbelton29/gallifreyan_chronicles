package com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.UUID;

public record TardisComponent(UUID tardisId, UUID ownerId, String ownerName) {
    public static final Codec<UUID> UUID_CODEC = Codec.STRING.xmap(UUID::fromString, UUID::toString);

    public static final Codec<TardisComponent> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            UUID_CODEC.fieldOf("tardis_id").forGetter(TardisComponent::tardisId),
            UUID_CODEC.fieldOf("owner_id").forGetter(TardisComponent::ownerId),
            Codec.STRING.optionalFieldOf("owner_name", "").forGetter(TardisComponent::ownerName)
    ).apply(inst, TardisComponent::new));
}

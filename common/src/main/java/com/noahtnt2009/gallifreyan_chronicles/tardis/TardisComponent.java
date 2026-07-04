package com.noahtnt2009.gallifreyan_chronicles.tardis;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.UUID;

public class TardisComponent {
    public static final Codec<UUID> UUID_CODEC = Codec.STRING.xmap(UUID::fromString, UUID::toString);

    public static final Codec<TardisComponent> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            UUID_CODEC.fieldOf("tardis_id").forGetter(TardisComponent::getTardisId),
            UUID_CODEC.fieldOf("owner_id").forGetter(TardisComponent::getOwnerId)
    ).apply(inst, TardisComponent::new));

    private final UUID tardisId;
    private final UUID ownerId;

    public TardisComponent(UUID tardisId, UUID ownerId) {
        this.tardisId = tardisId;
        this.ownerId = ownerId;
    }

    public UUID getTardisId() {
        return tardisId;
    }

    public UUID getOwnerId() {
        return ownerId;
    }
}

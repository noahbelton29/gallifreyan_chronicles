package com.noahtnt2009.gallifreyan_chronicles.tardis;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.noahtnt2009.gallifreyan_chronicles.registry.GCExteriors;
import com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.TardisExterior;
import com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.TardisExteriorRegistry;
import net.minecraft.core.BlockPos;

import java.util.UUID;

public class TardisComponent {
    public static final Codec<UUID> UUID_CODEC = Codec.STRING.xmap(UUID::fromString, UUID::toString);

    public static final Codec<TardisComponent> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            UUID_CODEC.fieldOf("tardis_id").forGetter(TardisComponent::getTardisId),
            UUID_CODEC.fieldOf("owner_id").forGetter(TardisComponent::getOwnerId),
            Codec.STRING.fieldOf("exterior").forGetter(c -> c.getExterior().id()),
            BlockPos.CODEC.optionalFieldOf("block_pos", null).forGetter(TardisComponent::getBlockPos)
    ).apply(inst, (tardisId, ownerId, exteriorId, blockPos) -> {
        TardisComponent component = new TardisComponent(tardisId, ownerId);
        component.setExterior(TardisExteriorRegistry.get(exteriorId));
        component.setBlockPos(blockPos);
        return component;
    }));

    private final UUID tardisId;
    private final UUID ownerId;
    private TardisExterior exterior;
    private BlockPos blockPos;

    public TardisComponent(UUID tardisId, UUID ownerId) {
        this.tardisId = tardisId;
        this.ownerId = ownerId;
        this.exterior = GCExteriors.POLICE_BOX_1963;
        this.blockPos = null;
    }

    public UUID getTardisId() { return tardisId; }
    public UUID getOwnerId() { return ownerId; }
    public TardisExterior getExterior() { return exterior; }
    public BlockPos getBlockPos() { return blockPos; }

    public void setExterior(TardisExterior exterior) {
        this.exterior = exterior;
    }

    public void setBlockPos(BlockPos blockPos) {
        this.blockPos = blockPos;
    }
}
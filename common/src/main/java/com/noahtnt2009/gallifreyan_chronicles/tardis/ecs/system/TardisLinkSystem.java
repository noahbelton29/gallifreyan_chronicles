package com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.system;

import com.noahtnt2009.gallifreyan_chronicles.ecs.Entity;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.TardisComponentTypes;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.component.TardisLinkComponent;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.component.TardisComponent;
import com.noahtnt2009.gallifreyan_chronicles.tardis.manager.TardisManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.Optional;
import java.util.UUID;

public final class TardisLinkSystem {
    private TardisLinkSystem() {
    }

    public static Optional<UUID> linkedTardisId(Entity entity) {
        TardisLinkComponent link = entity.getOrNull(TardisComponentTypes.TARDIS_LINK);
        return Optional.ofNullable(link).map(TardisLinkComponent::tardisId);
    }

    public static TardisComponent registerNewTardis(Entity entity, ServerLevel level, UUID ownerId, BlockPos pos) {
        TardisManager manager = TardisManager.get(level.getServer());
        TardisComponent record = manager.create(ownerId);
        manager.setBlockPos(record.getTardisId(), pos);

        entity.set(TardisComponentTypes.TARDIS_LINK, new TardisLinkComponent(record.getTardisId()));
        return record;
    }

    public static Optional<TardisComponent> resolve(Entity entity, ServerLevel level) {
        return linkedTardisId(entity).flatMap(id -> TardisManager.get(level.getServer()).get(id));
    }
}

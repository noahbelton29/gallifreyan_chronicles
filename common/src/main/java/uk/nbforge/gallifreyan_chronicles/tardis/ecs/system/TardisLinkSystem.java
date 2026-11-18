package uk.nbforge.gallifreyan_chronicles.tardis.ecs.system;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import uk.nbforge.gallifreyan_chronicles.ecs.Entity;
import uk.nbforge.gallifreyan_chronicles.tardis.TardisData;
import uk.nbforge.gallifreyan_chronicles.tardis.TardisManager;
import uk.nbforge.gallifreyan_chronicles.tardis.ecs.TardisComponentTypes;
import uk.nbforge.gallifreyan_chronicles.tardis.ecs.component.TardisComponent;
import uk.nbforge.gallifreyan_chronicles.tardis.ecs.component.TardisLinkComponent;

public final class TardisLinkSystem {
    private TardisLinkSystem() {}

    public static Optional<UUID> linkedTardisId(Entity entity) {
        TardisLinkComponent link = entity.getOrNull(TardisComponentTypes.TARDIS_LINK);
        return Optional.ofNullable(link).map(TardisLinkComponent::tardisId);
    }

    public static void link(Entity entity, UUID tardisId) {
        entity.set(TardisComponentTypes.TARDIS_LINK, new TardisLinkComponent(tardisId));
    }

    public static void unlink(Entity entity) {
        entity.remove(TardisComponentTypes.TARDIS_LINK);
    }

    public static TardisComponent registerNewTardis(
            Entity entity, ServerLevel level, UUID ownerId, String ownerName, BlockPos pos) {
        TardisData data = TardisManager.get(level.getServer()).createTardis(ownerId, ownerName);
        data.setRegisteredBlockPos(pos);
        data.setExteriorDimension(level.dimension());
        TardisComponent record = data.component();

        link(entity, record.tardisId());
        return record;
    }
}

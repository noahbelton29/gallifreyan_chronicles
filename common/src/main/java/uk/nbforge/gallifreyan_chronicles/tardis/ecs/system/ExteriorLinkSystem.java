package uk.nbforge.gallifreyan_chronicles.tardis.ecs.system;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import uk.nbforge.gallifreyan_chronicles.core.block_entities.TardisExteriorBlockEntity;
import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import uk.nbforge.gallifreyan_chronicles.tardis.TardisApi;
import uk.nbforge.gallifreyan_chronicles.tardis.TardisData;
import uk.nbforge.gallifreyan_chronicles.tardis.TardisManager;

public final class ExteriorLinkSystem {
    private ExteriorLinkSystem() {}

    private static Optional<TardisExteriorBlockEntity> findExterior(ServerLevel level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof TardisExteriorBlockEntity exterior) {
            return Optional.of(exterior);
        }
        return Optional.empty();
    }

    public static void link(ServerLevel level, UUID tardisId, BlockPos pos) {
        findExterior(level, pos).ifPresent(exterior -> exterior.linkToTardis(tardisId));
        TardisData data = TardisManager.get(level.getServer()).getTardis(tardisId);
        if (data == null) return;
        data.setExteriorBlockPos(pos);
        data.setExteriorDimension(level.dimension());
    }

    public static void unlink(ServerLevel level, UUID tardisId) {
        TardisApi.get(level.getServer(), tardisId)
                .flatMap(Tardis::exterior)
                .ifPresent(TardisExteriorBlockEntity::unlinkFromTardis);
        TardisData data = TardisManager.get(level.getServer()).getTardis(tardisId);
        if (data != null) data.setExteriorBlockPos(null);
    }
}

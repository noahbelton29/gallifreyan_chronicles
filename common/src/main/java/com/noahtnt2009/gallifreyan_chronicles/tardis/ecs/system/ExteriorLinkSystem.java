package com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.system;

import com.noahtnt2009.gallifreyan_chronicles.tardis.api.TardisApi;
import com.noahtnt2009.gallifreyan_chronicles.tardis.api.Tardis;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisExteriorBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.tardis.manager.TardisManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.Optional;
import java.util.UUID;

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
        TardisManager manager = TardisManager.get(level.getServer());
        manager.setExteriorBlockPos(tardisId, pos);
        manager.setExteriorDimension(tardisId, level.dimension());
    }

    public static void unlink(ServerLevel level, UUID tardisId) {
        TardisApi.get(level.getServer(), tardisId).flatMap(Tardis::exterior)
                .ifPresent(TardisExteriorBlockEntity::unlinkFromTardis);
        TardisManager.get(level.getServer()).unlinkExterior(tardisId);
    }
}

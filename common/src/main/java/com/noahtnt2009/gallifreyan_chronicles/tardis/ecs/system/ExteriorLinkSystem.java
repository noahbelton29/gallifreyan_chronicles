package com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.system;

import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisExteriorBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.tardis.manager.TardisManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.Optional;
import java.util.UUID;

public final class ExteriorLinkSystem {
    private ExteriorLinkSystem() {}

    public static Optional<BlockPos> resolvedExteriorPos(ServerLevel level, UUID tardisId) {
        return Optional.ofNullable(TardisManager.get(level.getServer()).resolveExteriorBlockPos(tardisId));
    }

    public static Optional<TardisExteriorBlockEntity> resolve(ServerLevel level, UUID tardisId) {
        return resolvedExteriorPos(level, tardisId).flatMap(pos -> findExterior(level, pos));
    }

    private static Optional<TardisExteriorBlockEntity> findExterior(ServerLevel level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof TardisExteriorBlockEntity exterior) {
            return Optional.of(exterior);
        }
        return Optional.empty();
    }

    public static void link(ServerLevel level, UUID tardisId, BlockPos pos) {
        findExterior(level, pos).ifPresent(exterior -> exterior.linkToTardis(tardisId));
        TardisManager.get(level.getServer()).setExteriorBlockPos(tardisId, pos);
    }

    public static void unlink(ServerLevel level, UUID tardisId) {
        resolve(level, tardisId).ifPresent(TardisExteriorBlockEntity::unlinkFromTardis);
        TardisManager.get(level.getServer()).unlinkExterior(tardisId);
    }
}

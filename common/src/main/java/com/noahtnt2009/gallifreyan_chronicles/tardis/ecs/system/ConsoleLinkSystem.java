package com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.system;

import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisConsoleBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.init.GCBlocks;
import com.noahtnt2009.gallifreyan_chronicles.tardis.manager.TardisManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;
import java.util.UUID;

public final class ConsoleLinkSystem {
    private ConsoleLinkSystem() {
    }

    public static Optional<BlockPos> linkedConsolePos(ServerLevel level, UUID tardisId) {
        return Optional.ofNullable(TardisManager.get(level.getServer()).getConsoleBlockPos(tardisId));
    }

    public static Optional<TardisConsoleBlockEntity> resolve(ServerLevel level, UUID tardisId) {
        return linkedConsolePos(level, tardisId).flatMap(pos -> findConsole(level, pos));
    }

    private static Optional<TardisConsoleBlockEntity> findConsole(ServerLevel level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof TardisConsoleBlockEntity console) {
            return Optional.of(console);
        }
        return Optional.empty();
    }

    public static void link(ServerLevel level, UUID tardisId, BlockPos pos) {
        findConsole(level, pos).ifPresent(console -> console.linkToTardis(tardisId));
        TardisManager.get(level.getServer()).setConsoleBlockPos(tardisId, pos);
    }

    public static void unlink(ServerLevel level, UUID tardisId) {
        resolve(level, tardisId).ifPresent(TardisConsoleBlockEntity::unlinkFromTardis);
        TardisManager.get(level.getServer()).unlinkConsole(tardisId);
    }

    public static Optional<TardisConsoleBlockEntity> spawnAndLink(ServerLevel level, UUID tardisId, BlockPos exteriorPos) {
        TardisManager manager = TardisManager.get(level.getServer());
        if (manager.getConsoleBlockPos(tardisId) != null) {
            return resolve(level, tardisId);
        }

        BlockPos consolePos = findConsolePlacementPos(level, exteriorPos);
        BlockState state = GCBlocks.TARDIS_CONSOLE.defaultBlockState();
        level.setBlock(consolePos, state, 3);

        if (!(level.getBlockEntity(consolePos) instanceof TardisConsoleBlockEntity console)) {
            return Optional.empty();
        }

        console.linkToTardis(tardisId);
        manager.setConsoleBlockPos(tardisId, consolePos);
        console.sync();

        return Optional.of(console);
    }

    private static BlockPos findConsolePlacementPos(ServerLevel level, BlockPos exteriorPos) {
        return exteriorPos.above();
    }
}


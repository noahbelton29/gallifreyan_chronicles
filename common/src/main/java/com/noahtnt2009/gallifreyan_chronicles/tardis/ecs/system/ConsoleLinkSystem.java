package com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.system;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.block.ConsoleBlock;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisConsoleBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisExteriorBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.init.GCBlocks;
import com.noahtnt2009.gallifreyan_chronicles.tardis.console.TardisConsole;
import com.noahtnt2009.gallifreyan_chronicles.tardis.console.TardisConsoleRegistry;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.impl.FlightLeverControl;
import com.noahtnt2009.gallifreyan_chronicles.tardis.manager.TardisManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;
import java.util.UUID;

public final class ConsoleLinkSystem {
    private ConsoleLinkSystem() {
    }

    private static final int CONSOLE_OFFSET_DISTANCE = 3;

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
        findConsole(level, pos).ifPresent(console -> {
            console.linkToTardis(tardisId);
            ConsoleSyncSystem.register(console);
        });
        TardisManager.get(level.getServer()).setConsoleBlockPos(tardisId, pos);
    }

    public static void unlink(ServerLevel level, UUID tardisId) {
        resolve(level, tardisId).ifPresent(TardisConsoleBlockEntity::unlinkFromTardis);
        TardisManager.get(level.getServer()).unlinkConsole(tardisId);
    }

    public static void spawnAndLink(ServerLevel level, UUID tardisId, BlockPos exteriorPos) {
        TardisManager manager = TardisManager.get(level.getServer());
        if (manager.getConsoleBlockPos(tardisId) != null) {
            resolve(level, tardisId).ifPresent(console -> console.linkToTardis(tardisId));
            return;
        }

        BlockPos consolePos = findConsolePlacementPos(level, exteriorPos);
        BlockState state = GCBlocks.TARDIS_CONSOLE.defaultBlockState();
        level.setBlock(consolePos, state, 3);

        if (!(level.getBlockEntity(consolePos) instanceof TardisConsoleBlockEntity console)) {
            return;
        }

        console.linkToTardis(tardisId);
        manager.setConsoleBlockPos(tardisId, consolePos);
        console.setConsole(TardisConsoleRegistry.get(TardisConsoleRegistry.defaultId()));

        spawnDefaultControls(console);

        console.sync();
    }

    public static void onPlaced(TardisConsoleBlockEntity console) {
        if (!(console.getLevel() instanceof ServerLevel level)) return;

        UUID tardisId = DoorSyncSystem.tardisIdOf(level);
        if (tardisId == null) return;

        TardisManager manager = TardisManager.get(level.getServer());
        if (manager.get(tardisId).isEmpty()) return;

        console.linkToTardis(tardisId);
        ConsoleSyncSystem.register(console);
        adoptAsPrimaryIfNeeded(level, manager, tardisId, console.getBlockPos());
        spawnDefaultControls(console);

        TardisConsoleBlockEntity sibling = null;
        for (TardisConsoleBlockEntity other : ConsoleSyncSystem.consolesOf(tardisId)) {
            if (other != console) {
                sibling = other;
                break;
            }
        }

        if (sibling != null) {
            console.applyCarriedControlStates(sibling.copyControlStates());
        } else {
            TardisExteriorBlockEntity exterior = DoorSyncSystem.findExterior(level.getServer(), tardisId);
            if (exterior != null && exterior.isTravelling()) {
                console.reconcileControl(FlightLeverControl.ID, true);
                console.triggerRotorAnimation("rotor_flight");
            }
        }

        DoorSyncSystem.reconcileConsole(level.getServer(), tardisId);
        console.sync();
    }

    private static void adoptAsPrimaryIfNeeded(ServerLevel level, TardisManager manager, UUID tardisId, BlockPos pos) {
        BlockPos current = manager.getConsoleBlockPos(tardisId);
        if (pos.equals(current)) return;

        boolean stale = current == null
                || (level.hasChunkAt(current) && !(level.getBlockState(current).getBlock() instanceof ConsoleBlock));
        if (stale) {
            manager.setConsoleBlockPos(tardisId, pos);
        }
    }

    public static void spawnDefaultControls(TardisConsoleBlockEntity console) {
        TardisConsole model = console.getConsole();
        if (model == null) {
            Constants.LOG.info("spawnDefaultControls: no console model set");
            return;
        }

        var entries = model.controls();
        Constants.LOG.info("spawnDefaultControls: consoleId={}, {} control entries", model.id(), entries.size());

        for (TardisConsole.ControlEntry entry : entries) {
            console.addControl(entry.id(), entry.offset(), entry.width(), entry.height(), entry.depth());
        }
    }


    private static BlockPos findConsolePlacementPos(ServerLevel level, BlockPos exteriorPos) {
        return exteriorPos.offset(CONSOLE_OFFSET_DISTANCE, 0, 0);
    }
}
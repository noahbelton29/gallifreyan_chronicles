package uk.nbforge.gallifreyan_chronicles.tardis.ecs.system;

import uk.nbforge.gallifreyan_chronicles.tardis.TardisApi;
import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.core.block.ConsoleBlock;
import uk.nbforge.gallifreyan_chronicles.core.block_entities.TardisConsoleBlockEntity;
import uk.nbforge.gallifreyan_chronicles.core.GCBlocks;
import uk.nbforge.gallifreyan_chronicles.core.GCTardisControls;
import uk.nbforge.gallifreyan_chronicles.tardis.console.TardisConsole;
import uk.nbforge.gallifreyan_chronicles.core.dynamic_registries.TardisConsoleRegistry;
import uk.nbforge.gallifreyan_chronicles.tardis.control.impl.FlightLeverControl;
import uk.nbforge.gallifreyan_chronicles.tardis.TardisManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;
import java.util.UUID;

public final class ConsoleLinkSystem {
    private ConsoleLinkSystem() {
    }

    private static final int CONSOLE_OFFSET_DISTANCE = 3;

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
        TardisApi.get(level.getServer(), tardisId).flatMap(Tardis::console)
                .ifPresent(TardisConsoleBlockEntity::unlinkFromTardis);
        TardisManager.get(level.getServer()).unlinkConsole(tardisId);
    }

    public static void spawnAndLink(ServerLevel level, UUID tardisId, BlockPos exteriorPos) {
        TardisManager manager = TardisManager.get(level.getServer());
        if (manager.getConsoleBlockPos(tardisId) != null) {
            TardisApi.get(level.getServer(), tardisId).flatMap(Tardis::console)
                    .ifPresent(console -> console.linkToTardis(tardisId));
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

        Tardis tardis = TardisApi.ofLevel(level).orElse(null);
        if (tardis == null) {
            /* placed outside a tardis: still give it its control hitboxes, they
               only animate (see TardisControlEntity) */
            spawnDefaultControls(console);
            console.sync();
            return;
        }
        UUID tardisId = tardis.id();

        TardisManager manager = tardis.data();

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
            console.reconcileControl(GCTardisControls.POWER_LEVER, false);
            if (tardis.isTravelling()) {
                console.reconcileControl(FlightLeverControl.ID, true);
                console.triggerRotorAnimation("rotor_flight");
            }
        }

        DoorSyncSystem.reconcileConsole(level.getServer(), tardisId);
        console.sync();
    }

    private static void adoptAsPrimaryIfNeeded(ServerLevel level, TardisManager manager, UUID tardisId, BlockPos pos) {
        BlockPos current = manager.getConsoleBlockPos(tardisId);
        assert current != null;
        if (pos.equals(current)) return;

        boolean stale = level.hasChunkAt(current) && !(level.getBlockState(current).getBlock() instanceof ConsoleBlock);
        if (stale) {
            manager.setConsoleBlockPos(tardisId, pos);
        }
    }

    public static void spawnDefaultControls(TardisConsoleBlockEntity console) {
        TardisConsole model = console.getConsole();
        if (model == null) {
            Constants.LOG.debug("spawnDefaultControls: no console model set");
            return;
        }

        var entries = model.controls();
        Constants.LOG.debug("spawnDefaultControls: consoleId={}, {} control entries", model.id(), entries.size());

        for (TardisConsole.ControlEntry entry : entries) {
            console.addControl(entry.id(), entry.offset(), entry.width(), entry.height(), entry.depth());
        }
    }


    private static BlockPos findConsolePlacementPos(ServerLevel level, BlockPos exteriorPos) {
        return exteriorPos.offset(CONSOLE_OFFSET_DISTANCE, 0, 0);
    }
}
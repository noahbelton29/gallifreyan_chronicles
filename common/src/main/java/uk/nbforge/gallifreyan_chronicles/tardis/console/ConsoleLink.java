package uk.nbforge.gallifreyan_chronicles.tardis.console;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import uk.nbforge.gallifreyan_chronicles.core.GCTardisControls;
import uk.nbforge.gallifreyan_chronicles.core.block.ConsoleBlock;
import uk.nbforge.gallifreyan_chronicles.core.block.registry.TardisBlocks;
import uk.nbforge.gallifreyan_chronicles.core.block_entity.TardisConsoleBlockEntity;
import uk.nbforge.gallifreyan_chronicles.core.data_registry.TardisConsoleRegistry;
import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import uk.nbforge.gallifreyan_chronicles.tardis.TardisData;
import uk.nbforge.gallifreyan_chronicles.tardis.door.DoorSync;
import uk.nbforge.gallifreyan_chronicles.util.GCLog;

public final class ConsoleLink {
    private ConsoleLink() {}

    private static final int CONSOLE_OFFSET_DISTANCE = 3;

    private static Optional<TardisConsoleBlockEntity> findConsole(ServerLevel level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof TardisConsoleBlockEntity console) {
            return Optional.of(console);
        }
        return Optional.empty();
    }

    public static void link(Tardis tardis, ServerLevel level, BlockPos pos) {
        findConsole(level, pos).ifPresent(console -> {
            console.linkToTardis(tardis.id());
            ConsoleSync.register(console);
        });
        tardis.data().setConsoleBlockPos(pos);
    }

    public static void unlink(Tardis tardis) {
        tardis.console().ifPresent(TardisConsoleBlockEntity::unlinkFromTardis);
        tardis.data().setConsoleBlockPos(null);
    }

    public static void spawnAndLink(Tardis tardis, ServerLevel level, BlockPos exteriorPos) {
        TardisData data = tardis.data();
        if (data.getConsoleBlockPos() != null) {
            tardis.console().ifPresent(console -> console.linkToTardis(tardis.id()));
            return;
        }

        BlockPos consolePos = findConsolePlacementPos(level, exteriorPos);
        BlockState state = TardisBlocks.TARDIS_CONSOLE.defaultBlockState();
        level.setBlock(consolePos, state, 3);

        if (!(level.getBlockEntity(consolePos) instanceof TardisConsoleBlockEntity console)) {
            return;
        }

        console.linkToTardis(tardis.id());
        data.setConsoleBlockPos(consolePos);
        console.setConsole(TardisConsoleRegistry.get(TardisConsoleRegistry.defaultId()));

        spawnDefaultControls(console);

        console.sync();
    }

    public static void onPlaced(TardisConsoleBlockEntity console) {
        if (!(console.getLevel() instanceof ServerLevel level)) return;

        Tardis tardis = Tardis.ofLevel(level).orElse(null);
        if (tardis == null) {
            // placed outside a tardis still gets its control hitboxes, they just animate, see TardisControlEntity
            spawnDefaultControls(console);
            console.sync();
            return;
        }
        UUID tardisId = tardis.id();

        TardisData data = tardis.data();

        console.linkToTardis(tardisId);
        ConsoleSync.register(console);
        adoptAsPrimaryIfNeeded(level, data, console.getBlockPos());
        spawnDefaultControls(console);

        TardisConsoleBlockEntity sibling = null;
        for (TardisConsoleBlockEntity other : tardis.consoles()) {
            if (other != console) {
                sibling = other;
                break;
            }
        }

        if (sibling != null) {
            console.applyCarriedControlStates(sibling.copyControlStates());
        } else {
            console.reconcileControl(GCTardisControls.POWER_CONTROL, false);
            if (tardis.isTravelling()) {
                console.reconcileControl(GCTardisControls.SPACE_TIME_THROTTLE, true);
                console.triggerRotorAnimation("rotor_flight");
            }
        }

        DoorSync.reconcileConsole(tardis);
        console.sync();
    }

    private static void adoptAsPrimaryIfNeeded(ServerLevel level, TardisData data, BlockPos pos) {
        BlockPos current = data.getConsoleBlockPos();
        assert current != null;
        if (pos.equals(current)) return;

        boolean stale =
                level.isLoaded(current) && !(level.getBlockState(current).getBlock() instanceof ConsoleBlock);
        if (stale) {
            data.setConsoleBlockPos(pos);
        }
    }

    public static void spawnDefaultControls(TardisConsoleBlockEntity console) {
        TardisConsole model = console.getConsole();
        if (model == null) {
            GCLog.TARDIS.debug("spawnDefaultControls: no console model set");
            return;
        }

        var entries = model.controls();
        GCLog.TARDIS.debug("spawnDefaultControls: consoleId={}, {} control entries", model.id(), entries.size());

        for (TardisConsole.ControlEntry entry : entries) {
            console.addControl(entry.id(), entry.offset(), entry.width(), entry.height(), entry.depth());
        }
    }

    private static BlockPos findConsolePlacementPos(ServerLevel level, BlockPos exteriorPos) {
        return exteriorPos.offset(CONSOLE_OFFSET_DISTANCE, 0, 0);
    }
}

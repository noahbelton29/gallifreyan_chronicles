package uk.nbforge.gallifreyan_chronicles.tardis.ecs.system;

import org.jspecify.annotations.Nullable;
import uk.nbforge.gallifreyan_chronicles.tardis.TardisApi;
import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import uk.nbforge.gallifreyan_chronicles.core.block.TardisBlock;
import uk.nbforge.gallifreyan_chronicles.core.block_entities.TardisExteriorBlockEntity;
import uk.nbforge.gallifreyan_chronicles.core.GCBlocks;
import uk.nbforge.gallifreyan_chronicles.tardis.flight.LandingTarget;
import uk.nbforge.gallifreyan_chronicles.tardis.flight.TardisLandingSite;
import uk.nbforge.gallifreyan_chronicles.tardis.TardisManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.UUID;

public final class TardisLandingSystem {
    private TardisLandingSystem() {
    }

    public static BlockPos findLandingSpot(ServerLevel level, BlockPos pos) {
        if (isStandable(level, pos)) return pos;

        int range = level.getMaxY() - level.getMinY();
        for (int d = 1; d <= range; d++) {
            BlockPos up = pos.above(d);
            if (isStandable(level, up)) return up;
            BlockPos down = pos.below(d);
            if (isStandable(level, down)) return down;
        }
        return pos;
    }

    private static boolean isStandable(ServerLevel level, BlockPos pos) {
        BlockPos below = pos.below();
        BlockPos above = pos.above();
        if (level.isOutsideBuildHeight(below) || level.isOutsideBuildHeight(above)) return false;

        return isOpen(level, pos) && isOpen(level, above)
                && level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
    }

    private static boolean isOpen(ServerLevel level, BlockPos pos) {
        var state = level.getBlockState(pos);
        return (state.canBeReplaced() || TardisLandingSite.isSoft(state)) && state.getFluidState().isEmpty();
    }

    public static void relocate(MinecraftServer server, UUID tardisId) {
        Tardis tardis = TardisApi.get(server, tardisId).orElse(null);
        if (tardis == null) return;
        LandingTarget target = tardis.landingTarget().orElse(null);
        if (target == null) return;

        ServerLevel newLevel = server.getLevel(target.dimension());
        TardisExteriorBlockEntity old = tardis.exterior().orElse(null);
        if (newLevel == null || old == null) return;

        BlockPos oldPos = old.getBlockPos();
        BlockPos pos = target.pos();
        if (newLevel.isOutsideBuildHeight(pos) || newLevel.isOutsideBuildHeight(pos.above())) return;
        newLevel.getChunkAt(pos);
        BlockPos own = newLevel == old.getLevel() ? oldPos : null;
        BlockPos found = TardisLandingSite.find(newLevel, pos, own);
        pos = found != null ? found : findLandingSpot(newLevel, pos);

        if (newLevel == old.getLevel() && pos.equals(oldPos)) {
            old.setYaw(target.yaw());
            tardis.data().setLandingTarget(tardisId, null);
            return;
        }

        TardisLandingSite.clearSoft(newLevel, pos);
        TardisLandingSite.clearSoft(newLevel, pos.above());
        if (!newLevel.getBlockState(pos).canBeReplaced() || !newLevel.getBlockState(pos.above()).canBeReplaced()) {
            return;
        }

        TardisExteriorBlockEntity fresh = moveExterior(tardis, newLevel, pos);
        if (fresh == null) return;

        fresh.setYaw(target.yaw());
        tardis.data().setLandingTarget(tardisId, null);
    }

    public static @Nullable TardisExteriorBlockEntity moveExterior(Tardis tardis, ServerLevel newLevel, BlockPos pos) {
        TardisExteriorBlockEntity old = tardis.exterior().orElse(null);
        if (old == null || !(old.getLevel() instanceof ServerLevel oldLevel)) return null;
        BlockPos oldPos = old.getBlockPos();

        boolean glowing = old.getBlockState().getValue(TardisBlock.GLOWING);

        BlockPos oldTopPos = oldPos.above();
        if (oldLevel.getBlockState(oldTopPos).is(GCBlocks.TARDIS_TOP)) {
            oldLevel.setBlock(oldTopPos, Blocks.AIR.defaultBlockState(),
                    Block.UPDATE_CLIENTS | Block.UPDATE_SUPPRESS_DROPS);
        }
        oldLevel.removeBlock(oldPos, false);

        newLevel.setBlock(
                pos,
                GCBlocks.TARDIS.defaultBlockState().setValue(TardisBlock.GLOWING, glowing),
                Block.UPDATE_ALL
        );
        if (!(newLevel.getBlockEntity(pos) instanceof TardisExteriorBlockEntity fresh)) return null;

        fresh.copyStateFrom(old);
        fresh.spawnKeyEntity();

        TardisManager manager = tardis.data();
        UUID id = tardis.id();
        manager.setRegisteredBlockPos(id, pos);
        manager.setExteriorDimension(id, newLevel.dimension());
        if (manager.getExteriorBlockPos(id) != null) {
            manager.setExteriorBlockPos(id, pos);
        }
        return fresh;
    }
}

package uk.nbforge.gallifreyan_chronicles.tardis.flight;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;
import uk.nbforge.gallifreyan_chronicles.core.block.TardisBlock;
import uk.nbforge.gallifreyan_chronicles.core.block.registry.TardisBlocks;
import uk.nbforge.gallifreyan_chronicles.core.block_entity.TardisExteriorBlockEntity;
import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import uk.nbforge.gallifreyan_chronicles.tardis.TardisData;

public final class TardisLanding {
    private TardisLanding() {}

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

        return isOpen(level, pos)
                && isOpen(level, above)
                && level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
    }

    private static boolean isOpen(ServerLevel level, BlockPos pos) {
        var state = level.getBlockState(pos);
        return (state.canBeReplaced() || TardisLandingSite.isSoft(state))
                && state.getFluidState().isEmpty();
    }

    public static void relocate(Tardis tardis) {
        LandingTarget target = tardis.landingTarget().orElse(null);
        if (target == null) return;

        ServerLevel newLevel = tardis.server().getLevel(target.dimension());
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
            tardis.data().setLandingTarget(null);
            return;
        }

        TardisLandingSite.clearSoft(newLevel, pos);
        TardisLandingSite.clearSoft(newLevel, pos.above());
        if (!newLevel.getBlockState(pos).canBeReplaced()
                || !newLevel.getBlockState(pos.above()).canBeReplaced()) {
            return;
        }

        TardisExteriorBlockEntity fresh = moveExterior(tardis, newLevel, pos);
        if (fresh == null) return;

        fresh.setYaw(target.yaw());
        tardis.data().setLandingTarget(null);
    }

    public static @Nullable TardisExteriorBlockEntity moveExterior(Tardis tardis, ServerLevel newLevel, BlockPos pos) {
        TardisExteriorBlockEntity old = tardis.exterior().orElse(null);
        if (old == null || !(old.getLevel() instanceof ServerLevel oldLevel)) return null;
        BlockPos oldPos = old.getBlockPos();

        BlockState oldState = old.getBlockState();
        boolean glowing = oldState.getValue(TardisBlock.GLOWING);
        boolean inFlight = oldState.getValue(TardisBlock.IN_FLIGHT);
        int light = oldState.getValue(TardisBlock.LIGHT);

        BlockPos oldTopPos = oldPos.above();
        if (oldLevel.getBlockState(oldTopPos).is(TardisBlocks.TARDIS_TOP)) {
            oldLevel.setBlock(
                    oldTopPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_SUPPRESS_DROPS);
        }
        oldLevel.removeBlock(oldPos, false);

        newLevel.setBlock(
                pos,
                TardisBlocks.TARDIS
                        .defaultBlockState()
                        .setValue(TardisBlock.GLOWING, glowing)
                        .setValue(TardisBlock.IN_FLIGHT, inFlight)
                        .setValue(TardisBlock.LIGHT, light),
                Block.UPDATE_ALL);
        if (!(newLevel.getBlockEntity(pos) instanceof TardisExteriorBlockEntity fresh)) return null;

        fresh.copyStateFrom(old);
        fresh.lock().spawnKeyEntity();

        TardisData data = tardis.data();
        data.setRegisteredBlockPos(pos);
        data.setExteriorDimension(newLevel.dimension());
        if (data.getExteriorBlockPos() != null) {
            data.setExteriorBlockPos(pos);
        }
        return fresh;
    }
}

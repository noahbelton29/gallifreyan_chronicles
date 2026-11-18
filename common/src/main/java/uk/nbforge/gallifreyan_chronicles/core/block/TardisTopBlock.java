package uk.nbforge.gallifreyan_chronicles.core.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.NonNull;
import uk.nbforge.gallifreyan_chronicles.core.block.registry.TardisBlocks;
import uk.nbforge.gallifreyan_chronicles.core.block_entity.TardisExteriorBlockEntity;
import uk.nbforge.gallifreyan_chronicles.tardis.exterior.ExteriorCollisionUtil;

public class TardisTopBlock extends Block {
    private static final VoxelShape SHAPE = Block.box(0, -16, 0, 16, 16, 16);

    public TardisTopBlock(Properties properties) {
        super(properties);
    }

    public static void placeAbove(Level level, BlockPos basePos) {
        BlockPos above = basePos.above();
        if (level.isOutsideBuildHeight(above)) return;

        BlockState existing = level.getBlockState(above);
        if (existing.is(TardisBlocks.TARDIS_TOP) || !existing.canBeReplaced()) return;

        level.setBlock(above, TardisBlocks.TARDIS_TOP.defaultBlockState(), Block.UPDATE_ALL);
    }

    public static BlockPos baseOf(Level level, BlockPos pos) {
        return level.getBlockState(pos).is(TardisBlocks.TARDIS_TOP) ? pos.below() : pos;
    }

    @Override
    protected @NonNull InteractionResult useWithoutItem(
            @NonNull BlockState state,
            @NonNull Level level,
            @NonNull BlockPos pos,
            @NonNull Player player,
            @NonNull BlockHitResult hitResult) {
        BlockPos below = pos.below();
        BlockState bottom = level.getBlockState(below);
        if (!bottom.is(TardisBlocks.TARDIS)) {
            if (!level.isClientSide()) level.removeBlock(pos, false);
            return InteractionResult.PASS;
        }
        return bottom.useWithoutItem(level, player, hitResult.withPosition(below));
    }

    @Override
    protected @NonNull VoxelShape getShape(
            @NonNull BlockState state,
            @NonNull BlockGetter level,
            @NonNull BlockPos pos,
            @NonNull CollisionContext context) {
        if (!(level.getBlockEntity(pos.below()) instanceof TardisExteriorBlockEntity tardis)
                || tardis.travel().isFullyInvisible()) {
            return Shapes.empty();
        }
        return SHAPE;
    }

    @Override
    protected @NonNull BlockState updateShape(
            @NonNull BlockState state,
            @NonNull LevelReader level,
            @NonNull ScheduledTickAccess ticks,
            @NonNull BlockPos pos,
            @NonNull Direction direction,
            @NonNull BlockPos neighborPos,
            @NonNull BlockState neighborState,
            @NonNull RandomSource random) {
        if (direction == Direction.DOWN && !neighborState.is(TardisBlocks.TARDIS)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, level, ticks, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    protected @NonNull VoxelShape getCollisionShape(
            @NonNull BlockState state,
            @NonNull BlockGetter level,
            @NonNull BlockPos pos,
            @NonNull CollisionContext context) {
        return ExteriorCollisionUtil.shape(level, pos.below());
    }

    @Override
    public @NonNull BlockState playerWillDestroy(
            @NonNull Level level, @NonNull BlockPos pos, @NonNull BlockState state, @NonNull Player player) {
        BlockPos below = pos.below();
        if (!level.isClientSide() && level.getBlockState(below).is(TardisBlocks.TARDIS)) {
            TardisBlock.deleteIfCreative(level, below, player);
            level.removeBlock(below, false);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected float getShadeBrightness(@NonNull BlockState state, @NonNull BlockGetter level, @NonNull BlockPos pos) {
        if (level.getBlockEntity(pos.below()) instanceof TardisExteriorBlockEntity tardis
                && !tardis.travel().isShadowActive()) {
            return 1.0f;
        }
        return super.getShadeBrightness(state, level, pos);
    }

    @Override
    public @NonNull RenderShape getRenderShape(@NonNull BlockState state) {
        return RenderShape.INVISIBLE;
    }
}

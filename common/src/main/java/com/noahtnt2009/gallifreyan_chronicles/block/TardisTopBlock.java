package com.noahtnt2009.gallifreyan_chronicles.block;

import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisExteriorBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.init.GCBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.NonNull;
import org.jetbrains.annotations.NotNull;

public class TardisTopBlock extends Block {
    public TardisTopBlock(Properties properties) {
        super(properties);
    }

    public static void placeAbove(Level level, BlockPos basePos) {
        BlockPos above = basePos.above();
        if (level.isOutsideBuildHeight(above)) return;

        BlockState existing = level.getBlockState(above);
        if (existing.is(GCBlocks.TARDIS_TOP) || !existing.canBeReplaced()) return;

        level.setBlock(above, GCBlocks.TARDIS_TOP.defaultBlockState(), Block.UPDATE_ALL);
    }

    public static BlockPos baseOf(Level level, BlockPos pos) {
        return level.getBlockState(pos).is(GCBlocks.TARDIS_TOP) ? pos.below() : pos;
    }

    @Override
    protected @NonNull InteractionResult useWithoutItem(@NonNull BlockState state, @NonNull Level level,
                                                        @NonNull BlockPos pos, @NonNull Player player,
                                                        @NonNull BlockHitResult hitResult) {
        BlockPos below = pos.below();
        BlockState bottom = level.getBlockState(below);
        if (!bottom.is(GCBlocks.TARDIS)) {
            if (!level.isClientSide()) level.removeBlock(pos, false);
            return InteractionResult.PASS;
        }
        return bottom.useWithoutItem(level, player, hitResult.withPosition(below));
    }

    @Override
    protected @NonNull VoxelShape getCollisionShape(@NonNull BlockState state, @NonNull BlockGetter level,
                                                    @NonNull BlockPos pos, @NonNull CollisionContext context) {
        if (level.getBlockEntity(pos.below()) instanceof TardisExteriorBlockEntity tardis
                && (tardis.isFullyInvisible() || !tardis.getDoorState().isClosed())) {
            return Shapes.empty();
        }
        return super.getCollisionShape(state, level, pos, context);
    }

    @Override
    public @NotNull BlockState playerWillDestroy(@NotNull Level level, @NotNull BlockPos pos,
                                                 @NotNull BlockState state, @NotNull Player player) {
        BlockPos below = pos.below();
        if (!level.isClientSide() && level.getBlockState(below).is(GCBlocks.TARDIS)) {
            level.removeBlock(below, false);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.INVISIBLE;
    }
}

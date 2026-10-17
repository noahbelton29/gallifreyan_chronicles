package com.noahtnt2009.gallifreyan_chronicles.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.NonNull;

public class GCFallingBlock extends FallingBlock {
    public GCFallingBlock(Properties properties) {
        super(properties);
    }

    @Override
    public int getDustColor(@NonNull BlockState blockState, @NonNull BlockGetter level, @NonNull BlockPos pos) {
        return 0xFFC3683F;
    }
}
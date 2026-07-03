package com.noahtnt2009.gallifreyan_chronicles.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.NonNull;

public class GCFallingBlock extends FallingBlock {
    public static final MapCodec<FallingBlock> CODEC = simpleCodec(GCFallingBlock::new);

    public GCFallingBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected @NonNull MapCodec<? extends FallingBlock> codec() {
        return CODEC;
    }

    @Override
    public int getDustColor(@NonNull BlockState blockState, @NonNull BlockGetter level, @NonNull BlockPos pos) {
        return 0xFFC3683F;
    }
}

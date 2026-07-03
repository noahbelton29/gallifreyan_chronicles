package com.noahtnt2009.gallifreyan_chronicles.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.NonNull;

public class ThorivineBlock extends CactusBlock {
    public ThorivineBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void entityInside(@NonNull BlockState state, Level level, @NonNull BlockPos pos, Entity entity,
                                @NonNull InsideBlockEffectApplier effectApplier, boolean isPrecise) {
        entity.hurt(level.damageSources().cactus(), 3.0F);
        if (entity instanceof LivingEntity livingEntity) {
            livingEntity.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 0));
            livingEntity.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 140, 0));
        }
    }

    @Override
    protected void randomTick(@NonNull BlockState state, ServerLevel level, BlockPos pos, @NonNull RandomSource random) {
        BlockPos above = pos.above();
        if (level.isEmptyBlock(above)) {
            int height = 1;
            int age = state.getValue(AGE);

            while (level.getBlockState(pos.below(height)).is(this)) {
                ++height;
                if (height == 3 && age == 15) {
                    return;
                }
            }

            if (age == 15 && height < 3) {
                level.setBlockAndUpdate(above, this.defaultBlockState());
                BlockState aboveBlock = state.setValue(AGE, 0);
                level.setBlock(pos, aboveBlock, 260);
                level.neighborChanged(aboveBlock, above, this, null, false);
            }

            if (age < 15) {
                level.setBlock(pos, state.setValue(AGE, age + 1), 260);
            }
        }
    }
}

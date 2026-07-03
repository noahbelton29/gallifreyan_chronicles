package com.noahtnt2009.gallifreyan_chronicles.world.feature;

import com.noahtnt2009.gallifreyan_chronicles.world.feature.config.BoulderFeatureConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

import java.util.List;

public class GCBoulderFeature extends Feature<BoulderFeatureConfig> {
    public GCBoulderFeature() {
        super(BoulderFeatureConfig.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<BoulderFeatureConfig> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        BoulderFeatureConfig config = context.config();

        List<Block> blocks = config.blocks();
        if (blocks.isEmpty()) {
            return false;
        }

        double radiusXZ = 2.5 + random.nextDouble() * 1.5;
        double radiusY = 2.0 + random.nextDouble() * 1.5;

        BlockPos center = origin.below((int) (radiusY * 0.4));

        int rx = (int) Math.ceil(radiusXZ);
        int ry = (int) Math.ceil(radiusY);

        for (int dx = -rx; dx <= rx; dx++) {
            for (int dy = -ry; dy <= ry; dy++) {
                for (int dz = -rx; dz <= rx; dz++) {

                    double jitter = (random.nextDouble() - 0.5) * 0.3;
                    double distSq =
                            (dx * dx) / (radiusXZ * radiusXZ)
                                    + (dy * dy) / (radiusY * radiusY)
                                    + (dz * dz) / (radiusXZ * radiusXZ);

                    if (distSq <= 1.0 + jitter) {
                        BlockPos pos = center.offset(dx, dy, dz);

                        BlockState state = blocks
                                .get(random.nextInt(blocks.size()))
                                .defaultBlockState();

                        level.setBlock(pos, state, 2);
                    }
                }
            }
        }

        return true;
    }
}
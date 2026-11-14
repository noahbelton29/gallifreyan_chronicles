package uk.nbforge.gallifreyan_chronicles.core.world.world_features;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import org.jspecify.annotations.NonNull;

import java.util.List;

import uk.nbforge.gallifreyan_chronicles.codec.world.WorldgenCodec;

public record GCBoulderFeature(List<Block> blocks) implements Feature {
    @Override
    public @NonNull MapCodec<GCBoulderFeature> codec() {
        return WorldgenCodec.GC_BOULDER_FEATURE;
    }

    @Override
    public boolean place(@NonNull WorldGenLevel level, @NonNull ChunkGenerator chunkGenerator, @NonNull RandomSource random, @NonNull BlockPos origin) {
        if (this.blocks.isEmpty()) {
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

                        BlockState state = this.blocks
                                .get(random.nextInt(this.blocks.size()))
                                .defaultBlockState();

                        level.setBlock(pos, state, 2);
                    }
                }
            }
        }

        return true;
    }
}
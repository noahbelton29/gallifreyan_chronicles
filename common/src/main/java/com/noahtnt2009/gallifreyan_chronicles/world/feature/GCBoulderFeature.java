package com.noahtnt2009.gallifreyan_chronicles.world.feature;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

import java.util.List;

public record GCBoulderFeature(List<Block> blocks) implements Feature {
    public static final MapCodec<GCBoulderFeature> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BuiltInRegistries.BLOCK.byNameCodec()
                    .listOf()
                    .fieldOf("blocks")
                    .forGetter(GCBoulderFeature::blocks)
    ).apply(instance, GCBoulderFeature::new));

    @Override
    public MapCodec<GCBoulderFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
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
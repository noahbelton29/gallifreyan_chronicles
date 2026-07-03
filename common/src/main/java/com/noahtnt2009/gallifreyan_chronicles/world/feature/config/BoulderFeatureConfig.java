package com.noahtnt2009.gallifreyan_chronicles.world.feature.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

import java.util.List;

public record BoulderFeatureConfig(List<Block> blocks) implements FeatureConfiguration {
    public static final Codec<BoulderFeatureConfig> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    BuiltInRegistries.BLOCK.byNameCodec()
                            .listOf()
                            .fieldOf("blocks")
                            .forGetter(BoulderFeatureConfig::blocks)
            ).apply(instance, BoulderFeatureConfig::new));
}
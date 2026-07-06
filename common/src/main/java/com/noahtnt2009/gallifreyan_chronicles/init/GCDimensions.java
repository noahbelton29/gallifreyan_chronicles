package com.noahtnt2009.gallifreyan_chronicles.init;

import com.mojang.datafixers.util.Pair;
import com.noahtnt2009.gallifreyan_chronicles.util.GCUtils;
import com.noahtnt2009.gallifreyan_chronicles.world.biome.GCSurfaceRules;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TimelineTags;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.attribute.AmbientSounds;
import net.minecraft.world.attribute.BackgroundMusic;
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.clock.WorldClocks;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.*;

import java.util.List;
import java.util.Optional;

public class GCDimensions {
    public static final ResourceKey<LevelStem> GALLIFREY_KEY = ResourceKey.create(Registries.LEVEL_STEM,
            GCUtils.of("gallifrey"));
    public static final ResourceKey<Level> GALLIFREY_LEVEL_KEY = ResourceKey.create(Registries.DIMENSION,
            GCUtils.of("gallifrey"));
    public static final ResourceKey<DimensionType> GALLIFREY_TYPE_KEY = ResourceKey.create(Registries.DIMENSION_TYPE,
            GCUtils.of("gallifrey_type"));
    public static final ResourceKey<NoiseGeneratorSettings> GALLIFREY_NOISE_SETTINGS = ResourceKey.create(Registries.NOISE_SETTINGS,
            GCUtils.of("gallifrey"));

    public static final ResourceKey<LevelStem> MOON_KEY = ResourceKey.create(Registries.LEVEL_STEM,
            GCUtils.of("moon"));
    public static final ResourceKey<Level> MOON_LEVEL_KEY = ResourceKey.create(Registries.DIMENSION,
            GCUtils.of("moon"));
    public static final ResourceKey<DimensionType> MOON_TYPE_KEY = ResourceKey.create(Registries.DIMENSION_TYPE,
            GCUtils.of("moon_type"));
    public static final ResourceKey<NoiseGeneratorSettings> MOON_NOISE_SETTINGS = ResourceKey.create(Registries.NOISE_SETTINGS,
            GCUtils.of("moon"));

    public static void bootstrapType(BootstrapContext<DimensionType> context) {
        var timelines = context.lookup(Registries.TIMELINE);
        var clocks = context.lookup(Registries.WORLD_CLOCK);
        var blocks = context.lookup(Registries.BLOCK);

        context.register(GALLIFREY_TYPE_KEY, new DimensionType(
                false,
                true,
                false,
                false,
                1.0,
                -64,
                384,
                384,
                blocks.getOrThrow(BlockTags.INFINIBURN_OVERWORLD),
                0.0f,
                new DimensionType.MonsterSettings(ConstantInt.of(0), 0),
                DimensionType.Skybox.OVERWORLD,
                CardinalLighting.Type.DEFAULT,
                EnvironmentAttributeMap.builder()
                        .set(EnvironmentAttributes.STAR_BRIGHTNESS, 0.0f)
                        .set(EnvironmentAttributes.FOG_COLOR, 0xFF9944)
                        .set(EnvironmentAttributes.SKY_COLOR, 0xFF6600)
                        .set(EnvironmentAttributes.SKY_LIGHT_COLOR, 0xFFDDBB)
                        .set(EnvironmentAttributes.SUNRISE_SUNSET_COLOR, 0xFFFFAA33)
                        .set(EnvironmentAttributes.CLOUD_COLOR, 0xFFFFC2A6)
                        .set(EnvironmentAttributes.CLOUD_HEIGHT, 192.0f)
                        .set(EnvironmentAttributes.BACKGROUND_MUSIC, BackgroundMusic.OVERWORLD)
                        .set(EnvironmentAttributes.AMBIENT_SOUNDS, AmbientSounds.LEGACY_CAVE_SETTINGS)
                        .set(EnvironmentAttributes.AMBIENT_LIGHT_COLOR, 0xFFD4A0)
                        .set(EnvironmentAttributes.SKY_FOG_END_DISTANCE, 256.0f)
                        .build(),
                timelines.getOrThrow(TimelineTags.IN_OVERWORLD),
                Optional.of(clocks.getOrThrow(WorldClocks.OVERWORLD))));

        context.register(MOON_TYPE_KEY, new DimensionType(
                false,
                true,
                false,
                false,
                1.0,
                -64,
                384,
                384,
                blocks.getOrThrow(BlockTags.INFINIBURN_OVERWORLD),
                0.0f,
                new DimensionType.MonsterSettings(ConstantInt.of(0), 0),
                DimensionType.Skybox.OVERWORLD,
                CardinalLighting.Type.DEFAULT,
                EnvironmentAttributeMap.builder()
                        .set(EnvironmentAttributes.STAR_BRIGHTNESS, 1.0f)
                        .set(EnvironmentAttributes.SKY_COLOR, 0xFF6600)
                        .set(EnvironmentAttributes.SKY_LIGHT_COLOR, 0xFFcccccc)
                        .set(EnvironmentAttributes.SUNRISE_SUNSET_COLOR, 0xFFcccccc)
                        .set(EnvironmentAttributes.CLOUD_COLOR, 0xcccccc)
                        .set(EnvironmentAttributes.CLOUD_HEIGHT, 192.0f)
                        .set(EnvironmentAttributes.BACKGROUND_MUSIC, BackgroundMusic.OVERWORLD)
                        .set(EnvironmentAttributes.AMBIENT_SOUNDS, AmbientSounds.LEGACY_CAVE_SETTINGS)
                        .set(EnvironmentAttributes.AMBIENT_LIGHT_COLOR, 0xFFcccccc)
                        .build(),
                timelines.getOrThrow(TimelineTags.IN_OVERWORLD),
                Optional.of(clocks.getOrThrow(WorldClocks.OVERWORLD))));
    }

    public static void bootstrapNoise(BootstrapContext<NoiseGeneratorSettings> context) {
        var biomes = context.lookup(Registries.BIOME);

        context.register(GALLIFREY_NOISE_SETTINGS, new NoiseGeneratorSettings(
                new NoiseSettings(-64, 384, 1, 2),
                GCBlocks.GALLIFREYAN_STONE.defaultBlockState(),
                Blocks.AIR.defaultBlockState(),
                NoiseRouterData.overworld(context.lookup(Registries.DENSITY_FUNCTION), context.lookup(Registries.NOISE), true, false),
                GCSurfaceRules.gallifrey(biomes),
                List.of(),
                0,
                false,
                false,
                false,
                false
        ));

        context.register(MOON_NOISE_SETTINGS, new NoiseGeneratorSettings(
                new NoiseSettings(-64, 384, 1, 2),
                GCBlocks.LUNAR_REGOLITH.defaultBlockState(),
                Blocks.AIR.defaultBlockState(),
                NoiseRouterData.overworld(context.lookup(Registries.DENSITY_FUNCTION), context.lookup(Registries.NOISE), true, false),
                GCSurfaceRules.moon(biomes),
                List.of(),
                0,
                false,
                false,
                false,
                false
        ));
    }

    public static void bootstrapStem(BootstrapContext<LevelStem> context) {
        var biomes = context.lookup(Registries.BIOME);
        var dimensionTypes = context.lookup(Registries.DIMENSION_TYPE);
        var noiseGenSettings = context.lookup(Registries.NOISE_SETTINGS);

        NoiseBasedChunkGenerator generator = new NoiseBasedChunkGenerator(
                MultiNoiseBiomeSource.createFromList(
                        new Climate.ParameterList<>(List.of(
                                Pair.of(
                                        Climate.parameters(
                                                Climate.Parameter.span(-0.9f, 0.1f),
                                                Climate.Parameter.span(0.2f, 1.0f),
                                                Climate.Parameter.span(-1.0f, -0.2f),
                                                Climate.Parameter.span(-0.4f, 0.4f),
                                                Climate.Parameter.span(-0.3f, 0.3f),
                                                Climate.Parameter.span(-1.0f, 1.0f),
                                                0.0f
                                        ),
                                        biomes.getOrThrow(GCBiomes.GALLIFREYAN_DESERT)
                                ),
                                Pair.of(
                                        Climate.parameters(
                                                Climate.Parameter.span(-0.6f, 0.5f),
                                                Climate.Parameter.span(-0.7f, 0.3f),
                                                Climate.Parameter.span(-0.2f, 0.7f),
                                                Climate.Parameter.span(-0.4f, 0.4f),
                                                Climate.Parameter.span(-0.3f, 0.3f),
                                                Climate.Parameter.span(-0.8f, 0.8f),
                                                0.0f
                                        ),
                                        biomes.getOrThrow(GCBiomes.GALLIFREYAN_PLAINS)
                                ),
                                Pair.of(Climate.parameters(
                                                Climate.Parameter.span(0.55f, 1.0f),
                                                Climate.Parameter.span(-1.0f, 1.0f),
                                                Climate.Parameter.span(0.03f, 1.0f),
                                                Climate.Parameter.span(-1.0f, -0.375f),
                                                Climate.Parameter.point(0.0f),
                                                Climate.Parameter.span(-1.0f, 1.0f),
                                                0.0f),
                                        biomes.getOrThrow(GCBiomes.GALLIFREYAN_BADLANDS))
                        ))),
                noiseGenSettings.getOrThrow(GCDimensions.GALLIFREY_NOISE_SETTINGS));

        NoiseBasedChunkGenerator generatorMoon = new NoiseBasedChunkGenerator(
                MultiNoiseBiomeSource.createFromList(
                        new Climate.ParameterList<>(List.of(
                                Pair.of(
                                        Climate.parameters(
                                                Climate.Parameter.span(-0.9f, 0.1f),
                                                Climate.Parameter.span(0.2f, 1.0f),
                                                Climate.Parameter.span(-1.0f, -0.2f),
                                                Climate.Parameter.span(-0.4f, 0.4f),
                                                Climate.Parameter.span(-0.3f, 0.3f),
                                                Climate.Parameter.span(-1.0f, 1.0f),
                                                0.0f
                                        ),
                                        biomes.getOrThrow(GCBiomes.MOON)
                                )
                        ))),
                noiseGenSettings.getOrThrow(GCDimensions.MOON_NOISE_SETTINGS));

        context.register(GALLIFREY_KEY,
                new LevelStem(dimensionTypes.getOrThrow(GALLIFREY_TYPE_KEY), generator));

        context.register(MOON_KEY,
                new LevelStem(dimensionTypes.getOrThrow(MOON_TYPE_KEY), generatorMoon));
    }
}
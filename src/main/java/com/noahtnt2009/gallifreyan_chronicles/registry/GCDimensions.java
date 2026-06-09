package com.noahtnt2009.gallifreyan_chronicles.registry;

import com.mojang.datafixers.util.Pair;
import com.noahtnt2009.gallifreyan_chronicles.GallifreyanChronicles;
import com.noahtnt2009.gallifreyan_chronicles.world.biome.GCSurfaceRules;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
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
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseRouterData;
import net.minecraft.world.level.levelgen.NoiseSettings;

import java.util.List;
import java.util.Optional;

public class GCDimensions {
    public static final ResourceKey<LevelStem> GALLIFREY_KEY = ResourceKey.create(Registries.LEVEL_STEM,
            Identifier.fromNamespaceAndPath(GallifreyanChronicles.MOD_ID, "gallifrey"));
    public static final ResourceKey<Level> GALLIFREY_LEVEL_KEY = ResourceKey.create(Registries.DIMENSION,
            Identifier.fromNamespaceAndPath(GallifreyanChronicles.MOD_ID, "gallifrey"));
    public static final ResourceKey<DimensionType> GALLIFREY_TYPE_KEY = ResourceKey.create(Registries.DIMENSION_TYPE,
            Identifier.fromNamespaceAndPath(GallifreyanChronicles.MOD_ID, "gallifrey_type"));
    public static final ResourceKey<NoiseGeneratorSettings> GALLIFREY_NOISE_SETTINGS = ResourceKey.create(Registries.NOISE_SETTINGS,
            Identifier.fromNamespaceAndPath(GallifreyanChronicles.MOD_ID, "gallifrey"));

    public static void bootstrapType(BootstrapContext<DimensionType> context) {
        var timelines = context.lookup(Registries.TIMELINE);
        var clocks = context.lookup(Registries.WORLD_CLOCK);

        context.register(GALLIFREY_TYPE_KEY, new DimensionType(
                false,
                true,
                false,
                false,
                1.0,
                -128,
                384,
                384,
                BlockTags.INFINIBURN_OVERWORLD,
                1.0f,
                new DimensionType.MonsterSettings(ConstantInt.of(0), 0),
                DimensionType.Skybox.OVERWORLD,
                CardinalLighting.Type.DEFAULT,
                EnvironmentAttributeMap.builder()
                        .set(EnvironmentAttributes.STAR_BRIGHTNESS, 0.0f)
                        .set(EnvironmentAttributes.FOG_COLOR, 0xFF9944)
                        .set(EnvironmentAttributes.SKY_COLOR, 0xFF6600)
                        .set(EnvironmentAttributes.SKY_LIGHT_COLOR, 0xFFDDBB)
                        .set(EnvironmentAttributes.SUNRISE_SUNSET_COLOR, 0xFFFFAA33)
                        .set(EnvironmentAttributes.CLOUD_COLOR, 0xFFCC66)
                        .set(EnvironmentAttributes.BACKGROUND_MUSIC, BackgroundMusic.OVERWORLD)
                        .set(EnvironmentAttributes.AMBIENT_SOUNDS, AmbientSounds.LEGACY_CAVE_SETTINGS)
                        .set(EnvironmentAttributes.AMBIENT_LIGHT_COLOR, 0xFFD4A0)
                        .build(),
                timelines.getOrThrow(TimelineTags.IN_OVERWORLD),
                Optional.of(clocks.getOrThrow(WorldClocks.OVERWORLD))));
    }

    public static void bootstrapNoise(BootstrapContext<NoiseGeneratorSettings> context) {
        context.register(GALLIFREY_NOISE_SETTINGS, new NoiseGeneratorSettings(
                new NoiseSettings(-128, 384, 1, 2),
                GCBlocks.GALLIFREYAN_STONE.defaultBlockState(),
                Blocks.AIR.defaultBlockState(),
                NoiseRouterData.overworld(context.lookup(Registries.DENSITY_FUNCTION), context.lookup(Registries.NOISE), true, false),
                GCSurfaceRules.gallifrey(),
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
                                Pair.of(Climate.parameters(
                                                Climate.Parameter.point(0.0f),
                                                Climate.Parameter.point(0.0f),
                                                Climate.Parameter.point(0.0f),
                                                Climate.Parameter.point(0.0f),
                                                Climate.Parameter.point(0.0f),
                                                Climate.Parameter.point(0.0f),
                                                0.0f),
                                        biomes.getOrThrow(GCBiomes.GALLIFREYAN_DESERT)),
                                Pair.of(Climate.parameters(
                                                Climate.Parameter.point(0.4f),
                                                Climate.Parameter.point(-0.3f),
                                                Climate.Parameter.point(0.0f),
                                                Climate.Parameter.point(0.0f),
                                                Climate.Parameter.point(0.0f),
                                                Climate.Parameter.point(0.375f),
                                                0.0f),
                                        biomes.getOrThrow(GCBiomes.GALLIFREYAN_PLAINS))
                        ))),
                noiseGenSettings.getOrThrow(GCDimensions.GALLIFREY_NOISE_SETTINGS));

        context.register(GALLIFREY_KEY,
                new LevelStem(dimensionTypes.getOrThrow(GALLIFREY_TYPE_KEY), generator));
    }
}
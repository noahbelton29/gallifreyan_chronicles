package com.noahtnt2009.gallifreyan_chronicles.init;

import com.mojang.datafixers.util.Pair;
import com.noahtnt2009.gallifreyan_chronicles.util.GCUtils;
import com.noahtnt2009.gallifreyan_chronicles.world.biome.GCMaterialRules;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TimelineTags;
import net.minecraft.util.ARGB;
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
import net.minecraft.world.level.levelgen.densityfunction.DensityFunction;

import java.util.List;
import java.util.Optional;

import static com.noahtnt2009.gallifreyan_chronicles.util.GCUtils.rgb;

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

    public static final ResourceKey<LevelStem> TARDIS_INTERIOR_KEY = ResourceKey.create(Registries.LEVEL_STEM,
            GCUtils.of("tardis_interior"));
    public static final ResourceKey<Level> TARDIS_INTERIOR_LEVEL_KEY = ResourceKey.create(Registries.DIMENSION,
            GCUtils.of("tardis_interior"));
    public static final ResourceKey<DimensionType> TARDIS_INTERIOR_TYPE_KEY = ResourceKey.create(Registries.DIMENSION_TYPE,
            GCUtils.of("tardis_interior_type"));

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
                        .set(EnvironmentAttributes.FOG_COLOR, rgb(0xFF9944))
                        .set(EnvironmentAttributes.SKY_COLOR, rgb(0xFF6600))
                        .set(EnvironmentAttributes.SKY_LIGHT_COLOR, rgb(0xFFDDBB))
                        .set(EnvironmentAttributes.SUNRISE_SUNSET_COLOR, ARGB.vector4fFromARGB32(0xFFFFAA33))
                        .set(EnvironmentAttributes.CLOUD_COLOR, ARGB.vector4fFromARGB32(0xFFFFC2A6))
                        .set(EnvironmentAttributes.CLOUD_HEIGHT, 192.0f)
                        .set(EnvironmentAttributes.BACKGROUND_MUSIC, BackgroundMusic.OVERWORLD)
                        .set(EnvironmentAttributes.AMBIENT_SOUNDS, AmbientSounds.LEGACY_CAVE_SETTINGS)
                        .set(EnvironmentAttributes.AMBIENT_LIGHT_COLOR, rgb(0xFFD4A0))
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
                        .set(EnvironmentAttributes.SKY_COLOR, rgb(0xFF6600))
                        .set(EnvironmentAttributes.SKY_LIGHT_COLOR, rgb(0xCCCCCC))
                        .set(EnvironmentAttributes.SUNRISE_SUNSET_COLOR, ARGB.vector4fFromARGB32(0xFFCCCCCC))
                        .set(EnvironmentAttributes.CLOUD_COLOR, ARGB.vector4fFromARGB32(0xFFCCCCCC))
                        .set(EnvironmentAttributes.CLOUD_HEIGHT, 192.0f)
                        .set(EnvironmentAttributes.BACKGROUND_MUSIC, BackgroundMusic.OVERWORLD)
                        .set(EnvironmentAttributes.AMBIENT_SOUNDS, AmbientSounds.LEGACY_CAVE_SETTINGS)
                        .set(EnvironmentAttributes.AMBIENT_LIGHT_COLOR, rgb(0xCCCCCC))
                        .build(),
                timelines.getOrThrow(TimelineTags.IN_OVERWORLD),
                Optional.of(clocks.getOrThrow(WorldClocks.OVERWORLD))));
    }

    public static void bootstrapNoise(BootstrapContext<NoiseGeneratorSettings> context) {
        var biomes = context.lookup(Registries.BIOME);
        HolderGetter<DensityFunction> functions = context.lookup(Registries.DENSITY_FUNCTION);
        var names = NoiseRouterData.OVERWORLD_FUNCTIONS;

        NoiseRouter overworldRouter = new NoiseRouter(
                NoiseRouterData.getFunction(functions, names.temperature()),
                NoiseRouterData.getFunction(functions, names.vegetation()),
                NoiseRouterData.getFunction(functions, names.continents()),
                NoiseRouterData.getFunction(functions, names.erosion()),
                NoiseRouterData.getFunction(functions, names.depth()),
                NoiseRouterData.getFunction(functions, NoiseRouterData.RIDGES),
                NoiseRouterData.getFunction(functions, names.chunkSurfaceLevel()),
                NoiseRouterData.getFunction(functions, names.finalDensity())
        );

        context.register(GALLIFREY_NOISE_SETTINGS, new NoiseGeneratorSettings(
                NoiseSettings.create(-64, 384),
                GCBlocks.GALLIFREYAN_STONE.defaultBlockState(),
                Blocks.AIR.defaultBlockState(),
                overworldRouter,
                Holder.direct(GCMaterialRules.gallifrey(biomes)),
                List.of(),
                0,
                false,
                Optional.empty(),
                false,
                NoiseGeneratorSettings.DebugFunctions.EMPTY
        ));

        context.register(MOON_NOISE_SETTINGS, new NoiseGeneratorSettings(
                NoiseSettings.create(-64, 384),
                GCBlocks.LUNAR_REGOLITH.defaultBlockState(),
                Blocks.AIR.defaultBlockState(),
                overworldRouter,
                Holder.direct(GCMaterialRules.moon(biomes)),
                List.of(),
                0,
                false,
                Optional.empty(),
                false,
                NoiseGeneratorSettings.DebugFunctions.EMPTY
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
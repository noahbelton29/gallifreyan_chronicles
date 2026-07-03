package com.noahtnt2009.gallifreyan_chronicles.world.biome;

import com.noahtnt2009.gallifreyan_chronicles.init.GCDimensions;
import com.noahtnt2009.gallifreyan_chronicles.world.GCPlacedFeatures;
import net.minecraft.core.HolderGetter;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class GCGallifreyBiomes {
    public static void addOres(BiomeGenerationSettings.Builder  biomeBuilder) {
        biomeBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, GCPlacedFeatures.IRON_GALLIFREY_ORES_UPPER_KEY);
        biomeBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, GCPlacedFeatures.IRON_GALLIFREY_ORES_MIDDLE_KEY);
        biomeBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, GCPlacedFeatures.IRON_GALLIFREY_ORES_SMALL_KEY);

        biomeBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, GCPlacedFeatures.COAL_GALLIFREY_ORES_LOWER_KEY);
        biomeBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, GCPlacedFeatures.COAL_GALLIFREY_ORES_UPPER_KEY);

        biomeBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, GCPlacedFeatures.GRAVEL_GALLIFREY_ORES_KEY);
        biomeBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, GCPlacedFeatures.COBBLED_LIMESTONE_GALLIFREY_ORES_KEY);
    }

    public static Biome gallifreyanDesert(
            HolderGetter<PlacedFeature> placedFeatureGetter,
            HolderGetter<ConfiguredWorldCarver<?>> carverGetter
    ) {
        MobSpawnSettings.Builder spawnBuilder = new MobSpawnSettings.Builder();
        BiomeGenerationSettings.Builder biomeBuilder =
                new BiomeGenerationSettings.Builder(placedFeatureGetter, carverGetter);
        BiomeDefaultFeatures.addDefaultCrystalFormations(biomeBuilder);
        BiomeDefaultFeatures.addDefaultMonsterRoom(biomeBuilder);
        BiomeDefaultFeatures.addDefaultUndergroundVariety(biomeBuilder);
        BiomeDefaultFeatures.addDefaultOres(biomeBuilder);

        biomeBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, GCPlacedFeatures.PATCH_GALLIFREY_DRY_GRASS);
        biomeBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, GCPlacedFeatures.IRON_GALLIFREY_ORES_UPPER_KEY);
        biomeBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, GCPlacedFeatures.IRON_GALLIFREY_ORES_MIDDLE_KEY);
        biomeBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, GCPlacedFeatures.IRON_GALLIFREY_ORES_SMALL_KEY);

        biomeBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, GCPlacedFeatures.COAL_GALLIFREY_ORES_LOWER_KEY);
        biomeBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, GCPlacedFeatures.COAL_GALLIFREY_ORES_UPPER_KEY);

        biomeBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, GCPlacedFeatures.GRAVEL_GALLIFREY_ORES_KEY);
        biomeBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, GCPlacedFeatures.COBBLED_LIMESTONE_GALLIFREY_ORES_KEY);
        biomeBuilder.addFeature(GenerationStep.Decoration.TOP_LAYER_MODIFICATION, GCPlacedFeatures.GALLIFREYAN_STONE_BOULDER);
        biomeBuilder.addFeature(GenerationStep.Decoration.TOP_LAYER_MODIFICATION, GCPlacedFeatures.GALLIFREYAN_STONE_PATCH);

        biomeBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, GCPlacedFeatures.PATCH_THORIVINE);

        return new Biome.BiomeBuilder()
                .hasPrecipitation(false)
                .temperature(4.0F)
                .downfall(0.0F)
                .specialEffects(new BiomeSpecialEffects.Builder()
                        .waterColor(0xBDB133)
                        .build())
                .setAttribute(EnvironmentAttributes.SKY_COLOR, 0xFF8833)
                .setAttribute(EnvironmentAttributes.AMBIENT_LIGHT_COLOR, 0xFF9944)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 0xFF9944)
                .setAttribute(EnvironmentAttributes.FOG_START_DISTANCE, 40.0F)
                .setAttribute(EnvironmentAttributes.FOG_END_DISTANCE, 100.0F)
                .mobSpawnSettings(spawnBuilder.build())
                .generationSettings(biomeBuilder.build())
                .build();
    }

    public static Biome gallifreyanPlains(
            HolderGetter<PlacedFeature> placedFeatureGetter,
            HolderGetter<ConfiguredWorldCarver<?>> carverGetter
    ) {
        MobSpawnSettings.Builder spawnBuilder = new MobSpawnSettings.Builder();
        BiomeGenerationSettings.Builder biomeBuilder =
                new BiomeGenerationSettings.Builder(placedFeatureGetter, carverGetter);
        BiomeDefaultFeatures.addDefaultCrystalFormations(biomeBuilder);
        BiomeDefaultFeatures.addDefaultMonsterRoom(biomeBuilder);
        BiomeDefaultFeatures.addDefaultUndergroundVariety(biomeBuilder);
        BiomeDefaultFeatures.addDefaultOres(biomeBuilder);

        biomeBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION,
                GCPlacedFeatures.CADONWOOD_TREE_PLACED);

        biomeBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION,
                GCPlacedFeatures.CADONWOOD_FALLEN_LOG);

        biomeBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION,
                GCPlacedFeatures.PATCH_GALLIFREY_TALL_GRASS_PLAIN);

        biomeBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION,
                GCPlacedFeatures.PATCH_GALLIFREY_TALL_GRASS_PLAIN_2);

        biomeBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION,
                GCPlacedFeatures.PATCH_GALLIFREY_GRASS_PLAIN);

        biomeBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, GCPlacedFeatures.IRON_GALLIFREY_ORES_UPPER_KEY);
        biomeBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, GCPlacedFeatures.IRON_GALLIFREY_ORES_MIDDLE_KEY);
        biomeBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, GCPlacedFeatures.IRON_GALLIFREY_ORES_SMALL_KEY);

        biomeBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, GCPlacedFeatures.COAL_GALLIFREY_ORES_LOWER_KEY);
        biomeBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, GCPlacedFeatures.COAL_GALLIFREY_ORES_UPPER_KEY);

        biomeBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, GCPlacedFeatures.GRAVEL_GALLIFREY_ORES_KEY);
        biomeBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, GCPlacedFeatures.COBBLED_LIMESTONE_GALLIFREY_ORES_KEY);

        return new Biome.BiomeBuilder()
                .hasPrecipitation(false)
                .temperature(2.0F)
                .downfall(0.1F)
                .specialEffects(new BiomeSpecialEffects.Builder()
                        .waterColor(0xBDB133)
                        .grassColorOverride(0xE0E0E0)
                        .build())
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 0xFF590a0a)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, 0xFF790d0d)
                .setAttribute(EnvironmentAttributes.AMBIENT_LIGHT_COLOR, 0xFF590a0a)
                .setAttribute(EnvironmentAttributes.FOG_START_DISTANCE, 40.0F)    // When fog starts appearing
                .setAttribute(EnvironmentAttributes.FOG_END_DISTANCE, 60.0F)
                .mobSpawnSettings(spawnBuilder.build())
                .generationSettings(biomeBuilder.build())
                .build();
    }

    public static Biome gallifreyanBadlands(
            HolderGetter<PlacedFeature> placedFeatureGetter,
            HolderGetter<ConfiguredWorldCarver<?>> carverGetter
    ) {
        MobSpawnSettings.Builder spawnBuilder = new MobSpawnSettings.Builder();
        BiomeGenerationSettings.Builder biomeBuilder =
                new BiomeGenerationSettings.Builder(placedFeatureGetter, carverGetter);
        BiomeDefaultFeatures.addDefaultCrystalFormations(biomeBuilder);
        BiomeDefaultFeatures.addDefaultMonsterRoom(biomeBuilder);
        BiomeDefaultFeatures.addDefaultUndergroundVariety(biomeBuilder);
        BiomeDefaultFeatures.addDefaultOres(biomeBuilder);

        addOres(biomeBuilder);

        biomeBuilder.addFeature(GenerationStep.Decoration.TOP_LAYER_MODIFICATION, GCPlacedFeatures.GALLIFREYAN_ARCADIAN_SHALE_BOULDER);
        biomeBuilder.addFeature(GenerationStep.Decoration.TOP_LAYER_MODIFICATION, GCPlacedFeatures.GALLIFREYAN_MUDSTONE_PATCH);

        return new Biome.BiomeBuilder()
                .hasPrecipitation(false)
                .temperature(3.5F)
                .downfall(0.0F)
                .specialEffects(new BiomeSpecialEffects.Builder()
                        .waterColor(0xBDB133)
                        .build())
                .setAttribute(EnvironmentAttributes.SKY_COLOR, 0xFF6622)
                .setAttribute(EnvironmentAttributes.AMBIENT_LIGHT_COLOR, 0xFF7733)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 0xFF7733)
                .setAttribute(EnvironmentAttributes.FOG_START_DISTANCE, 30.0F)
                .setAttribute(EnvironmentAttributes.FOG_END_DISTANCE, 80.0F)
                .mobSpawnSettings(spawnBuilder.build())
                .generationSettings(biomeBuilder.build())
                .build();
    }
}
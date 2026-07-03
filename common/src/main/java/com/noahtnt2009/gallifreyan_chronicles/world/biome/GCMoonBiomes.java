package com.noahtnt2009.gallifreyan_chronicles.world.biome;

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

public class GCMoonBiomes {
    public static Biome moon(
            HolderGetter<PlacedFeature> placedFeatureGetter,
            HolderGetter<ConfiguredWorldCarver<?>> carverGetter
    ) {
        MobSpawnSettings.Builder spawnBuilder = new MobSpawnSettings.Builder();
        BiomeGenerationSettings.Builder biomeBuilder =
                new BiomeGenerationSettings.Builder(placedFeatureGetter, carverGetter);
        BiomeDefaultFeatures.addDefaultUndergroundVariety(biomeBuilder);
        BiomeDefaultFeatures.addDefaultOres(biomeBuilder);

        biomeBuilder.addFeature(GenerationStep.Decoration.TOP_LAYER_MODIFICATION, GCPlacedFeatures.MOON_BOULDER);

        return new Biome.BiomeBuilder()
                .hasPrecipitation(false)
                .temperature(4.0F)
                .downfall(0.0F)
                .specialEffects(new BiomeSpecialEffects.Builder()
                        .waterColor(0xBDB133)
                        .build())
                .setAttribute(EnvironmentAttributes.SKY_COLOR, 0xFF000000)
                .setAttribute(EnvironmentAttributes.AMBIENT_LIGHT_COLOR, 0xFFcccccc)
                .mobSpawnSettings(spawnBuilder.build())
                .generationSettings(biomeBuilder.build())
                .build();
    }
}

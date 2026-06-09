package com.noahtnt2009.gallifreyan_chronicles.world.gen;

import com.noahtnt2009.gallifreyan_chronicles.registry.GCBiomes;
import com.noahtnt2009.gallifreyan_chronicles.world.GCPlacedFeatures;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.world.level.levelgen.GenerationStep;

public class GCBushGeneration {
    public static void generateBushes() {
        BiomeModifications.addFeature(BiomeSelectors.includeByKey(GCBiomes.GALLIFREYAN_DESERT),
                GenerationStep.Decoration.VEGETAL_DECORATION, GCPlacedFeatures.PATCH_GALLIFREY_DRY_GRASS);

        BiomeModifications.addFeature(BiomeSelectors.includeByKey(GCBiomes.GALLIFREYAN_PLAINS), GenerationStep.Decoration.VEGETAL_DECORATION, GCPlacedFeatures.PATCH_GALLIFREY_GRASS_PLAIN);
        BiomeModifications.addFeature(BiomeSelectors.includeByKey(GCBiomes.GALLIFREYAN_PLAINS), GenerationStep.Decoration.VEGETAL_DECORATION, GCPlacedFeatures.PATCH_GALLIFREY_TALL_GRASS_PLAIN);
        BiomeModifications.addFeature(BiomeSelectors.includeByKey(GCBiomes.GALLIFREYAN_PLAINS), GenerationStep.Decoration.VEGETAL_DECORATION, GCPlacedFeatures.PATCH_GALLIFREY_TALL_GRASS_PLAIN_2);
    }
}

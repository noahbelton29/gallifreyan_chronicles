package uk.nbforge.gallifreyan_chronicles.core.world.gen;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.world.level.levelgen.GenerationStep;
import uk.nbforge.gallifreyan_chronicles.core.GCBiomes;
import uk.nbforge.gallifreyan_chronicles.core.world.world_features.GCPlacedFeatures;
import uk.nbforge.gallifreyan_chronicles.util.GCLog;

public class GCBushGeneration {
    public static void generateBushes() {
        BiomeModifications.addFeature(
                BiomeSelectors.includeByKey(GCBiomes.GALLIFREYAN_DESERT),
                GenerationStep.Decoration.VEGETAL_DECORATION,
                GCPlacedFeatures.PATCH_GALLIFREY_DRY_GRASS);

        BiomeModifications.addFeature(
                BiomeSelectors.includeByKey(GCBiomes.GALLIFREYAN_DESERT),
                GenerationStep.Decoration.VEGETAL_DECORATION,
                GCPlacedFeatures.PATCH_THORIVINE);

        BiomeModifications.addFeature(
                BiomeSelectors.includeByKey(GCBiomes.GALLIFREYAN_PLAINS),
                GenerationStep.Decoration.VEGETAL_DECORATION,
                GCPlacedFeatures.PATCH_GALLIFREY_GRASS_PLAIN);
        BiomeModifications.addFeature(
                BiomeSelectors.includeByKey(GCBiomes.GALLIFREYAN_PLAINS),
                GenerationStep.Decoration.VEGETAL_DECORATION,
                GCPlacedFeatures.PATCH_GALLIFREY_TALL_GRASS_PLAIN);
        BiomeModifications.addFeature(
                BiomeSelectors.includeByKey(GCBiomes.GALLIFREYAN_PLAINS),
                GenerationStep.Decoration.VEGETAL_DECORATION,
                GCPlacedFeatures.PATCH_GALLIFREY_TALL_GRASS_PLAIN_2);

        GCLog.CORE.info("Registered GC Bush Generation (Fabric)");
    }
}

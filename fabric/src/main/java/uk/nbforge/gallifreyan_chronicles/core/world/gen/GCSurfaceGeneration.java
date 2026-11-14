package uk.nbforge.gallifreyan_chronicles.core.world.gen;

import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.core.GCBiomes;
import uk.nbforge.gallifreyan_chronicles.core.world.world_features.GCPlacedFeatures;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.world.level.levelgen.GenerationStep;

public class GCSurfaceGeneration {
    public static void generateSurface() {
        BiomeModifications.addFeature(BiomeSelectors.includeByKey(GCBiomes.GALLIFREYAN_DESERT),
                GenerationStep.Decoration.TOP_LAYER_MODIFICATION, GCPlacedFeatures.GALLIFREYAN_STONE_BOULDER);
        BiomeModifications.addFeature(BiomeSelectors.includeByKey(GCBiomes.GALLIFREYAN_BADLANDS),
                GenerationStep.Decoration.TOP_LAYER_MODIFICATION, GCPlacedFeatures.GALLIFREYAN_ARCADIAN_SHALE_BOULDER);
        BiomeModifications.addFeature(BiomeSelectors.includeByKey(GCBiomes.GALLIFREYAN_BADLANDS),
                GenerationStep.Decoration.TOP_LAYER_MODIFICATION, GCPlacedFeatures.GALLIFREYAN_MUDSTONE_PATCH);
        BiomeModifications.addFeature(BiomeSelectors.includeByKey(GCBiomes.GALLIFREYAN_DESERT),
                GenerationStep.Decoration.TOP_LAYER_MODIFICATION, GCPlacedFeatures.GALLIFREYAN_STONE_PATCH);

        BiomeModifications.addFeature(BiomeSelectors.includeByKey(GCBiomes.MOON),
                GenerationStep.Decoration.TOP_LAYER_MODIFICATION, GCPlacedFeatures.MOON_BOULDER);

        Constants.LOG.info("Registered GC Surface Generation (Fabric)");
    }
}

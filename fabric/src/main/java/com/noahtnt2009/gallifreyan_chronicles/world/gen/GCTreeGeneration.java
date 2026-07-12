package com.noahtnt2009.gallifreyan_chronicles.world.gen;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.init.GCBiomes;
import com.noahtnt2009.gallifreyan_chronicles.world.GCPlacedFeatures;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.world.level.levelgen.GenerationStep;

public class GCTreeGeneration {
    public static void generateTrees() {
        BiomeModifications.addFeature(BiomeSelectors.includeByKey(GCBiomes.GALLIFREYAN_PLAINS),
                GenerationStep.Decoration.VEGETAL_DECORATION, GCPlacedFeatures.CADONWOOD_TREE_PLACED);
        BiomeModifications.addFeature(BiomeSelectors.includeByKey(GCBiomes.GALLIFREYAN_PLAINS),
                GenerationStep.Decoration.VEGETAL_DECORATION, GCPlacedFeatures.CADONWOOD_FALLEN_LOG);
        BiomeModifications.addFeature(BiomeSelectors.includeByKey(GCBiomes.GALLIFREYAN_DESERT),
                GenerationStep.Decoration.VEGETAL_DECORATION, GCPlacedFeatures.PATCH_THORIVINE);

        Constants.LOG.info("Registered GC Tree Generation (Fabric)");
    }
}

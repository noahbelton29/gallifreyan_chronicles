package com.noahtnt2009.gallifreyan_chronicles.world.gen;

import com.noahtnt2009.gallifreyan_chronicles.registry.GCBiomes;
import com.noahtnt2009.gallifreyan_chronicles.world.GCPlacedFeatures;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.world.level.levelgen.GenerationStep;

public class GCTreeGeneration {
    public static void generateTrees() {
        BiomeModifications.addFeature(BiomeSelectors.includeByKey(GCBiomes.GALLIFREYAN_PLAINS),
                GenerationStep.Decoration.VEGETAL_DECORATION, GCPlacedFeatures.CADONWOOD_TREE_PLACED);
    }
}

package com.noahtnt2009.gallifreyan_chronicles.world.gen;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.init.GCDimensions;
import com.noahtnt2009.gallifreyan_chronicles.world.feature.GCPlacedFeatures;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.List;

public class GCOresGeneration {
    private static final List<ResourceKey<PlacedFeature>> GALLIFREY_ORES = List.of(
            GCPlacedFeatures.IRON_GALLIFREY_ORES_UPPER_KEY,
            GCPlacedFeatures.IRON_GALLIFREY_ORES_MIDDLE_KEY,
            GCPlacedFeatures.IRON_GALLIFREY_ORES_SMALL_KEY,
            GCPlacedFeatures.COAL_GALLIFREY_ORES_UPPER_KEY,
            GCPlacedFeatures.COAL_GALLIFREY_ORES_LOWER_KEY,
            GCPlacedFeatures.GRAVEL_GALLIFREY_ORES_KEY
    );

    public static void generateOres() {
        for (ResourceKey<PlacedFeature> ore : GALLIFREY_ORES) {
            BiomeModifications.addFeature(
                    context -> context.canGenerateIn(GCDimensions.GALLIFREY_KEY),
                    GenerationStep.Decoration.UNDERGROUND_ORES,
                    ore
            );
        }

        Constants.LOG.info("Registered GC Ore Generation (Fabric)");
    }
}

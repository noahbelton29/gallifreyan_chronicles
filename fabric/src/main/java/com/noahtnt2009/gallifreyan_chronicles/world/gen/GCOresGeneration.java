package com.noahtnt2009.gallifreyan_chronicles.world.gen;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.init.GCDimensions;
import com.noahtnt2009.gallifreyan_chronicles.world.GCPlacedFeatures;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.minecraft.world.level.levelgen.GenerationStep;

public class GCOresGeneration {
    public static void generateOres() {
        BiomeModifications.addFeature(
                context -> context.canGenerateIn(GCDimensions.GALLIFREY_KEY),
                GenerationStep.Decoration.UNDERGROUND_ORES,
                GCPlacedFeatures.IRON_GALLIFREY_ORES_UPPER_KEY
        );
        BiomeModifications.addFeature(
                context -> context.canGenerateIn(GCDimensions.GALLIFREY_KEY),
                GenerationStep.Decoration.UNDERGROUND_ORES,
                GCPlacedFeatures.IRON_GALLIFREY_ORES_MIDDLE_KEY
        );
        BiomeModifications.addFeature(
                context -> context.canGenerateIn(GCDimensions.GALLIFREY_KEY),
                GenerationStep.Decoration.UNDERGROUND_ORES,
                GCPlacedFeatures.IRON_GALLIFREY_ORES_SMALL_KEY
        );
        BiomeModifications.addFeature(
                context -> context.canGenerateIn(GCDimensions.GALLIFREY_KEY),
                GenerationStep.Decoration.UNDERGROUND_ORES,
                GCPlacedFeatures.COAL_GALLIFREY_ORES_UPPER_KEY
        );
        BiomeModifications.addFeature(
                context -> context.canGenerateIn(GCDimensions.GALLIFREY_KEY),
                GenerationStep.Decoration.UNDERGROUND_ORES,
                GCPlacedFeatures.COAL_GALLIFREY_ORES_LOWER_KEY
        );
        BiomeModifications.addFeature(
                context -> context.canGenerateIn(GCDimensions.GALLIFREY_KEY),
                GenerationStep.Decoration.UNDERGROUND_ORES,
                GCPlacedFeatures.GRAVEL_GALLIFREY_ORES_KEY
        );

        Constants.LOG.info("Registered GC Ore Generation (Fabric)");
    }
}
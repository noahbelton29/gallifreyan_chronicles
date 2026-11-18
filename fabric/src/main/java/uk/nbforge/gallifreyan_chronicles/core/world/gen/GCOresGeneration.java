package uk.nbforge.gallifreyan_chronicles.core.world.gen;

import java.util.List;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import uk.nbforge.gallifreyan_chronicles.core.GCDimensions;
import uk.nbforge.gallifreyan_chronicles.core.world.world_features.GCPlacedFeatures;
import uk.nbforge.gallifreyan_chronicles.util.GCLog;

public class GCOresGeneration {
    private static final List<ResourceKey<PlacedFeature>> GALLIFREY_ORES = List.of(
            GCPlacedFeatures.IRON_GALLIFREY_ORES_UPPER_KEY,
            GCPlacedFeatures.IRON_GALLIFREY_ORES_MIDDLE_KEY,
            GCPlacedFeatures.IRON_GALLIFREY_ORES_SMALL_KEY,
            GCPlacedFeatures.COAL_GALLIFREY_ORES_UPPER_KEY,
            GCPlacedFeatures.COAL_GALLIFREY_ORES_LOWER_KEY,
            GCPlacedFeatures.GRAVEL_GALLIFREY_ORES_KEY);

    public static void generateOres() {
        for (ResourceKey<PlacedFeature> ore : GALLIFREY_ORES) {
            BiomeModifications.addFeature(
                    context -> context.canGenerateIn(GCDimensions.GALLIFREY_KEY),
                    GenerationStep.Decoration.UNDERGROUND_ORES,
                    ore);
        }

        GCLog.CORE.info("Registered GC Ore Generation (Fabric)");
    }
}

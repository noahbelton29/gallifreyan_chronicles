package uk.nbforge.gallifreyan_chronicles.core;

import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.core.world.gen.GCBushGeneration;
import uk.nbforge.gallifreyan_chronicles.core.world.gen.GCOresGeneration;
import uk.nbforge.gallifreyan_chronicles.core.world.gen.GCSurfaceGeneration;
import uk.nbforge.gallifreyan_chronicles.core.world.gen.GCTreeGeneration;

public class GCFabricWorldGeneration {
    public static void registerWorldGeneration() {
        GCTreeGeneration.generateTrees();
        GCBushGeneration.generateBushes();
        GCOresGeneration.generateOres();
        GCSurfaceGeneration.generateSurface();
        Constants.LOG.info("Registered GC World Generation");
    }
}

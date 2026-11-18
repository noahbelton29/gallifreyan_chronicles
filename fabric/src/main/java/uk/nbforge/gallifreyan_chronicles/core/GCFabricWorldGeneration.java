package uk.nbforge.gallifreyan_chronicles.core;

import uk.nbforge.gallifreyan_chronicles.core.world.gen.GCBushGeneration;
import uk.nbforge.gallifreyan_chronicles.core.world.gen.GCOresGeneration;
import uk.nbforge.gallifreyan_chronicles.core.world.gen.GCSurfaceGeneration;
import uk.nbforge.gallifreyan_chronicles.core.world.gen.GCTreeGeneration;
import uk.nbforge.gallifreyan_chronicles.util.GCLog;

public class GCFabricWorldGeneration {
    public static void registerWorldGeneration() {
        GCTreeGeneration.generateTrees();
        GCBushGeneration.generateBushes();
        GCOresGeneration.generateOres();
        GCSurfaceGeneration.generateSurface();
        GCLog.CORE.info("Registered GC World Generation");
    }
}

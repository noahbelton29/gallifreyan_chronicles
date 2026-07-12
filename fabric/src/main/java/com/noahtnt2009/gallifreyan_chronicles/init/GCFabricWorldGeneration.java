package com.noahtnt2009.gallifreyan_chronicles.init;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.world.gen.GCBushGeneration;
import com.noahtnt2009.gallifreyan_chronicles.world.gen.GCOresGeneration;
import com.noahtnt2009.gallifreyan_chronicles.world.gen.GCSurfaceGeneration;
import com.noahtnt2009.gallifreyan_chronicles.world.gen.GCTreeGeneration;

public class GCFabricWorldGeneration {
    public static void registerWorldGeneration() {
        GCTreeGeneration.generateTrees();
        GCBushGeneration.generateBushes();
        GCOresGeneration.generateOres();
        GCSurfaceGeneration.generateSurface();
        Constants.LOG.info("Registered GC World Generation");
    }
}

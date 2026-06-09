package com.noahtnt2009.gallifreyan_chronicles.registry;

import com.noahtnt2009.gallifreyan_chronicles.world.gen.GCBushGeneration;
import com.noahtnt2009.gallifreyan_chronicles.world.gen.GCOresGeneration;
import com.noahtnt2009.gallifreyan_chronicles.world.gen.GCTreeGeneration;

public class GCWorldGeneration {
    public static void init() {
        GCOresGeneration.generateOres();
        GCBushGeneration.generateBushes();
        GCTreeGeneration.generateTrees();
    }
}

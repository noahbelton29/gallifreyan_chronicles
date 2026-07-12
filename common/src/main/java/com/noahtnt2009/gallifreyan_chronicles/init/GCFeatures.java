package com.noahtnt2009.gallifreyan_chronicles.init;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.util.GCUtils;
import com.noahtnt2009.gallifreyan_chronicles.world.feature.GCBoulderFeature;
import com.noahtnt2009.gallifreyan_chronicles.world.feature.config.BoulderFeatureConfig;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.feature.Feature;

public class GCFeatures {
    public static final Feature<BoulderFeatureConfig> BOULDER =
            Registry.register(BuiltInRegistries.FEATURE,
                    GCUtils.of("boulder"),
                    new GCBoulderFeature());

    public static void registerFeatures() {
        Constants.LOG.info("Registered GC Features");
    }
}

package com.noahtnt2009.gallifreyan_chronicles.init;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.world.feature.GCBoulderFeature;
import com.noahtnt2009.gallifreyan_chronicles.world.feature.config.BoulderFeatureConfig;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class GCFeatures {
    public static final Feature<BoulderFeatureConfig> BOULDER =
            Registry.register(BuiltInRegistries.FEATURE,
                    Identifier.fromNamespaceAndPath(Constants.MOD_ID, "boulder"),
                    new GCBoulderFeature());

    public static void registerFeatures() {
        Constants.LOG.info("GC Features registered");
    }
}

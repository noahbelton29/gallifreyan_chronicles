package com.noahtnt2009.gallifreyan_chronicles.init;

import com.mojang.serialization.MapCodec;
import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.util.GCUtils;
import com.noahtnt2009.gallifreyan_chronicles.world.feature.GCBoulderFeature;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public class GCFeatures {
    public static final MapCodec<GCBoulderFeature> BOULDER =
            Registry.register(BuiltInRegistries.FEATURE_TYPE,
                    GCUtils.modIdentifier("boulder"),
                    GCBoulderFeature.CODEC);

    public static void registerFeatures() {
        Constants.LOG.info("Registered GC Features");
    }
}
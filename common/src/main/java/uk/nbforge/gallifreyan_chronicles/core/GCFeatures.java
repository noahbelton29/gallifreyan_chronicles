package uk.nbforge.gallifreyan_chronicles.core;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import uk.nbforge.gallifreyan_chronicles.core.world.world_features.BoulderFeature;
import uk.nbforge.gallifreyan_chronicles.util.GCLog;
import uk.nbforge.gallifreyan_chronicles.util.GCUtil;

public class GCFeatures {
    public static final MapCodec<BoulderFeature> BOULDER =
            Registry.register(BuiltInRegistries.FEATURE_TYPE, GCUtil.modIdentifier("boulder"), BoulderFeature.CODEC);

    public static void registerFeatures() {
        GCLog.CORE.info("Registered GC Features");
    }
}

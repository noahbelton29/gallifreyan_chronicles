package uk.nbforge.gallifreyan_chronicles.core;

import com.mojang.serialization.MapCodec;
import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.util.GCUtil;
import uk.nbforge.gallifreyan_chronicles.core.world.world_features.GCBoulderFeature;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import uk.nbforge.gallifreyan_chronicles.codec.world.WorldgenCodec;

public class GCFeatures {
    public static final MapCodec<GCBoulderFeature> BOULDER =
            Registry.register(BuiltInRegistries.FEATURE_TYPE,
                    GCUtil.modIdentifier("boulder"),
                    WorldgenCodec.GC_BOULDER_FEATURE);

    public static void registerFeatures() {
        Constants.LOG.info("Registered GC Features");
    }
}
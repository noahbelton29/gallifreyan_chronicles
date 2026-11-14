package uk.nbforge.gallifreyan_chronicles.loader.sky;

import uk.nbforge.gallifreyan_chronicles.loader.GCJsonReloadListener;
import uk.nbforge.gallifreyan_chronicles.network.DimensionSkySyncPayload;
import uk.nbforge.gallifreyan_chronicles.platform.Services;
import net.minecraft.server.MinecraftServer;

import uk.nbforge.gallifreyan_chronicles.core.dynamic_registries.DimensionSkyRegistry;
import uk.nbforge.gallifreyan_chronicles.sky.data.DimensionSky;
import uk.nbforge.gallifreyan_chronicles.codec.sky.data.DimensionSkyCodec;

public class DimensionSkyLoader extends GCJsonReloadListener<DimensionSky> {
    public DimensionSkyLoader() {
        super(DimensionSkyCodec.CODEC, "dimension_sky", "Dimension Sky");
    }

    @Override
    protected void clearRegistry() {
        DimensionSkyRegistry.clear();
    }

    @Override
    protected void register(DimensionSky value) {
        DimensionSkyRegistry.register(value);
    }

    @Override
    protected int registrySize() {
        return DimensionSkyRegistry.size();
    }

    @Override
    protected void broadcastSync(MinecraftServer server) {
        Services.NETWORK.broadcastDimensionSkySync(server, DimensionSkySyncPayload.create());
    }
}

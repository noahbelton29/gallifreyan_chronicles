package uk.nbforge.gallifreyan_chronicles.loader.sky;

import uk.nbforge.gallifreyan_chronicles.loader.GCJsonReloadListener;
import uk.nbforge.gallifreyan_chronicles.network.DimensionSkySyncPayload;
import uk.nbforge.gallifreyan_chronicles.platform.Services;
import net.minecraft.server.MinecraftServer;

import uk.nbforge.gallifreyan_chronicles.core.dynamic_registries.CelestialBodyRegistry;
import uk.nbforge.gallifreyan_chronicles.sky.data.CelestialBody;
import uk.nbforge.gallifreyan_chronicles.codec.sky.data.CelestialBodyCodec;

public class CelestialBodyLoader extends GCJsonReloadListener<CelestialBody> {
    public CelestialBodyLoader() {
        super(CelestialBodyCodec.CODEC, "celestial_body", "Celestial Body");
    }

    @Override
    protected void clearRegistry() {
        CelestialBodyRegistry.clear();
    }

    @Override
    protected void register(CelestialBody value) {
        CelestialBodyRegistry.register(value);
    }

    @Override
    protected int registrySize() {
        return CelestialBodyRegistry.size();
    }

    @Override
    protected void broadcastSync(MinecraftServer server) {
        Services.NETWORK.broadcastDimensionSkySync(server, DimensionSkySyncPayload.create());
    }
}

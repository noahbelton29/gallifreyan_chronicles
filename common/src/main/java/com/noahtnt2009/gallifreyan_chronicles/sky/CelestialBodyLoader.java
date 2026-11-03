package com.noahtnt2009.gallifreyan_chronicles.sky;

import com.noahtnt2009.gallifreyan_chronicles.data.GCJsonReloadListener;
import com.noahtnt2009.gallifreyan_chronicles.network.DimensionSkySyncPayload;
import com.noahtnt2009.gallifreyan_chronicles.platform.Services;
import net.minecraft.server.MinecraftServer;

public class CelestialBodyLoader extends GCJsonReloadListener<CelestialBody> {
    public CelestialBodyLoader() {
        super(CelestialBody.CODEC, "celestial_body", "Celestial Body");
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

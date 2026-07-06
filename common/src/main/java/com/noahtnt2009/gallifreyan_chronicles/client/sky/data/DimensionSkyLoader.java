package com.noahtnt2009.gallifreyan_chronicles.client.sky.data;

import com.noahtnt2009.gallifreyan_chronicles.data.GCJsonReloadListener;
import com.noahtnt2009.gallifreyan_chronicles.network.DimensionSkySyncPayload;
import com.noahtnt2009.gallifreyan_chronicles.platform.Services;
import net.minecraft.server.MinecraftServer;

public class DimensionSkyLoader extends GCJsonReloadListener<DimensionSky> {
    public DimensionSkyLoader() {
        super(DimensionSky.CODEC, "dimension_sky", "Dimension Sky");
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

package com.noahtnt2009.gallifreyan_chronicles.network;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.client.sky.data.DimensionSky;
import com.noahtnt2009.gallifreyan_chronicles.client.sky.data.DimensionSkyRegistry;

public final class DimensionSkySyncHandler {
    private DimensionSkySyncHandler() {
    }

    public static void apply(DimensionSkySyncPayload payload) {
        DimensionSkyRegistry.clear();
        for (DimensionSky sky : payload.skies()) {
            DimensionSkyRegistry.register(sky);
        }
        Constants.LOG.info("Synced {} dimension sky(ies) to client", payload.skies().size());
    }
}
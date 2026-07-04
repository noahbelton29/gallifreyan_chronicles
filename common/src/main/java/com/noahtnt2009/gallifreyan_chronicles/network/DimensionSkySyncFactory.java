package com.noahtnt2009.gallifreyan_chronicles.network;

import com.noahtnt2009.gallifreyan_chronicles.client.sky.data.DimensionSkyRegistry;

import java.util.List;

public final class DimensionSkySyncFactory {
    private DimensionSkySyncFactory() {
    }

    public static DimensionSkySyncPayload create() {
        return new DimensionSkySyncPayload(List.copyOf(DimensionSkyRegistry.getAll()));
    }
}
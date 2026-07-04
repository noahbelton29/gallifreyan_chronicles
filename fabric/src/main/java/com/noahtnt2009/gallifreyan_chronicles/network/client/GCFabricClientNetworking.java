package com.noahtnt2009.gallifreyan_chronicles.network.client;

import com.noahtnt2009.gallifreyan_chronicles.network.DimensionSkySyncHandler;
import com.noahtnt2009.gallifreyan_chronicles.network.DimensionSkySyncPayload;
import com.noahtnt2009.gallifreyan_chronicles.network.TardisExteriorSyncHandler;
import com.noahtnt2009.gallifreyan_chronicles.network.TardisExteriorSyncPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class GCFabricClientNetworking {
    private GCFabricClientNetworking() {
    }

    public static void registerClientNetworking() {
        ClientPlayNetworking.registerGlobalReceiver(TardisExteriorSyncPayload.TYPE,
                (payload, context) -> context.client().execute(() -> TardisExteriorSyncHandler.apply(payload)));

        ClientPlayNetworking.registerGlobalReceiver(DimensionSkySyncPayload.TYPE,
                (payload, context) -> context.client().execute(() -> DimensionSkySyncHandler.apply(payload)));
    }
}
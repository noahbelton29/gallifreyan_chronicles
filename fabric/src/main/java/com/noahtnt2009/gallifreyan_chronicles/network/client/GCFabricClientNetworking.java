package com.noahtnt2009.gallifreyan_chronicles.network.client;

import com.noahtnt2009.gallifreyan_chronicles.network.DimensionSkySyncPayload;
import com.noahtnt2009.gallifreyan_chronicles.network.TardisConsoleSyncPayload;
import com.noahtnt2009.gallifreyan_chronicles.network.TardisExteriorSyncPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class GCFabricClientNetworking {
    private GCFabricClientNetworking() {
    }

    public static void registerClientNetworking() {
        ClientPlayNetworking.registerGlobalReceiver(TardisExteriorSyncPayload.TYPE,
                (payload, context) -> context.client().execute(payload::apply));

        ClientPlayNetworking.registerGlobalReceiver(TardisConsoleSyncPayload.TYPE,
                (payload, context) -> context.client().execute(payload::apply));

        ClientPlayNetworking.registerGlobalReceiver(DimensionSkySyncPayload.TYPE,
                (payload, context) -> context.client().execute(payload::apply));
    }
}

package com.noahtnt2009.gallifreyan_chronicles.network;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.client.sky.data.DimensionSkyLoader;
import com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.TardisExteriorLoader;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public final class GCFabricNetworking {
    private GCFabricNetworking() {
    }

    public static void registerNetworking() {
        PayloadTypeRegistry.clientboundPlay().register(TardisExteriorSyncPayload.TYPE, TardisExteriorSyncPayload.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(DimensionSkySyncPayload.TYPE, DimensionSkySyncPayload.STREAM_CODEC);

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            TardisExteriorLoader.setCurrentServer(server);
            DimensionSkyLoader.setCurrentServer(server);
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            sender.sendPacket(TardisExteriorSyncFactory.create());
            sender.sendPacket(DimensionSkySyncFactory.create());
        });

        Constants.LOG.info("Registered GC networking (Fabric)");
    }
}
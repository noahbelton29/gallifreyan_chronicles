package com.noahtnt2009.gallifreyan_chronicles.platform.services;

import com.noahtnt2009.gallifreyan_chronicles.network.DimensionSkySyncPayload;
import com.noahtnt2009.gallifreyan_chronicles.network.TardisExteriorSyncPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public interface INetworkHelper {
    void sendTardisExteriorSync(ServerPlayer player, TardisExteriorSyncPayload payload);
    void broadcastTardisExteriorSync(MinecraftServer server, TardisExteriorSyncPayload payload);
    void sendDimensionSkySync(ServerPlayer player, DimensionSkySyncPayload payload);
    void broadcastDimensionSkySync(MinecraftServer server, DimensionSkySyncPayload payload);
}

package com.noahtnt2009.gallifreyan_chronicles.platform;

import com.noahtnt2009.gallifreyan_chronicles.network.TardisExteriorSyncPayload;
import com.noahtnt2009.gallifreyan_chronicles.platform.services.INetworkHelper;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class FabricNetworkHelper implements INetworkHelper {
    @Override
    public void sendTardisExteriorSync(ServerPlayer player, TardisExteriorSyncPayload payload) {
        if (ServerPlayNetworking.canSend(player, TardisExteriorSyncPayload.TYPE)) {
            ServerPlayNetworking.send(player, payload);
        }
    }

    @Override
    public void broadcastTardisExteriorSync(MinecraftServer server, TardisExteriorSyncPayload payload) {
        for (ServerPlayer player : PlayerLookup.all(server)) {
            sendTardisExteriorSync(player, payload);
        }
    }
}

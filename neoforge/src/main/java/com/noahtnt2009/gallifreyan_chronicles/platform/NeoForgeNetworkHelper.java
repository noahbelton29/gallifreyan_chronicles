package com.noahtnt2009.gallifreyan_chronicles.platform;

import com.noahtnt2009.gallifreyan_chronicles.network.TardisExteriorSyncPayload;
import com.noahtnt2009.gallifreyan_chronicles.platform.services.INetworkHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public class NeoForgeNetworkHelper implements INetworkHelper {
    @Override
    public void sendTardisExteriorSync(ServerPlayer player, TardisExteriorSyncPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    @Override
    public void broadcastTardisExteriorSync(MinecraftServer server, TardisExteriorSyncPayload payload) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            sendTardisExteriorSync(player, payload);
        }
    }
}

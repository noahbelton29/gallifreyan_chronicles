package uk.nbforge.gallifreyan_chronicles.platform.services;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.Nullable;

public interface INetworkHelper {
    void send(ServerPlayer player, CustomPacketPayload payload);

    default void broadcast(MinecraftServer server, CustomPacketPayload payload) {
        broadcast(server, payload, null);
    }

    default void broadcast(MinecraftServer server, CustomPacketPayload payload, @Nullable ServerPlayer excluded) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (excluded != null && player.getUUID().equals(excluded.getUUID())) continue;
            send(player, payload);
        }
    }
}

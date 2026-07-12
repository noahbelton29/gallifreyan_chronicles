package com.noahtnt2009.gallifreyan_chronicles.platform;

import com.noahtnt2009.gallifreyan_chronicles.network.DimensionSkySyncPayload;
import com.noahtnt2009.gallifreyan_chronicles.network.TardisConsoleAnimationPayload;
import com.noahtnt2009.gallifreyan_chronicles.network.TardisConsoleRotorStatePayload;
import com.noahtnt2009.gallifreyan_chronicles.network.TardisConsoleSyncPayload;
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

    @Override
    public void sendDimensionSkySync(ServerPlayer player, DimensionSkySyncPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    @Override
    public void broadcastDimensionSkySync(MinecraftServer server, DimensionSkySyncPayload payload) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            sendDimensionSkySync(player, payload);
        }
    }

    @Override
    public void sendTardisConsoleSync(ServerPlayer player, TardisConsoleSyncPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    @Override
    public void broadcastTardisConsoleSync(MinecraftServer server, TardisConsoleSyncPayload payload) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            sendTardisConsoleSync(player, payload);
        }
    }

    @Override
    public void sendTardisConsoleAnimation(ServerPlayer player, TardisConsoleAnimationPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    @Override
    public void broadcastTardisConsoleAnimation(MinecraftServer server, TardisConsoleAnimationPayload payload) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            sendTardisConsoleAnimation(player, payload);
        }
    }

    @Override
    public void broadcastTardisConsoleAnimation(MinecraftServer server, TardisConsoleAnimationPayload payload, ServerPlayer excludedPlayer) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (excludedPlayer != null && player.getUUID().equals(excludedPlayer.getUUID())) continue;
            sendTardisConsoleAnimation(player, payload);
        }
    }

    @Override
    public void sendTardisConsoleRotorState(ServerPlayer player, TardisConsoleRotorStatePayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    @Override
    public void broadcastTardisConsoleRotorState(MinecraftServer server, TardisConsoleRotorStatePayload payload) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            sendTardisConsoleRotorState(player, payload);
        }
    }
}
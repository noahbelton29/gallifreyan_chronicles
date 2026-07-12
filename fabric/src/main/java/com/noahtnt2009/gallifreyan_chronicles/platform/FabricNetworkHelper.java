package com.noahtnt2009.gallifreyan_chronicles.platform;

import com.noahtnt2009.gallifreyan_chronicles.network.DimensionSkySyncPayload;
import com.noahtnt2009.gallifreyan_chronicles.network.TardisConsoleAnimationPayload;
import com.noahtnt2009.gallifreyan_chronicles.network.TardisConsoleRotorStatePayload;
import com.noahtnt2009.gallifreyan_chronicles.network.TardisConsoleSyncPayload;
import com.noahtnt2009.gallifreyan_chronicles.network.TardisExteriorSyncPayload;
import com.noahtnt2009.gallifreyan_chronicles.platform.services.INetworkHelper;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class FabricNetworkHelper implements INetworkHelper {
    @Override
    public void sendTardisExteriorSync(ServerPlayer player, TardisExteriorSyncPayload payload) {
        ServerPlayNetworking.send(player, payload);
    }

    @Override
    public void broadcastTardisExteriorSync(MinecraftServer server, TardisExteriorSyncPayload payload) {
        for (ServerPlayer player : PlayerLookup.all(server)) {
            sendTardisExteriorSync(player, payload);
        }
    }

    @Override
    public void sendDimensionSkySync(ServerPlayer player, DimensionSkySyncPayload payload) {
        ServerPlayNetworking.send(player, payload);
    }

    @Override
    public void broadcastDimensionSkySync(MinecraftServer server, DimensionSkySyncPayload payload) {
        for (ServerPlayer player : PlayerLookup.all(server)) {
            sendDimensionSkySync(player, payload);
        }
    }

    @Override
    public void sendTardisConsoleSync(ServerPlayer player, TardisConsoleSyncPayload payload) {
        ServerPlayNetworking.send(player, payload);
    }

    @Override
    public void broadcastTardisConsoleSync(MinecraftServer server, TardisConsoleSyncPayload payload) {
        for (ServerPlayer player : PlayerLookup.all(server)) {
            sendTardisConsoleSync(player, payload);
        }
    }

    @Override
    public void sendTardisConsoleAnimation(ServerPlayer player, TardisConsoleAnimationPayload payload) {
        ServerPlayNetworking.send(player, payload);
    }

    @Override
    public void broadcastTardisConsoleAnimation(MinecraftServer server, TardisConsoleAnimationPayload payload) {
        for (ServerPlayer player : PlayerLookup.all(server)) {
            sendTardisConsoleAnimation(player, payload);
        }
    }

    @Override
    public void broadcastTardisConsoleAnimation(MinecraftServer server, TardisConsoleAnimationPayload payload, ServerPlayer excludedPlayer) {
        for (ServerPlayer player : PlayerLookup.all(server)) {
            if (excludedPlayer != null && player.getUUID().equals(excludedPlayer.getUUID())) continue;
            sendTardisConsoleAnimation(player, payload);
        }
    }

    @Override
    public void sendTardisConsoleRotorState(ServerPlayer player, TardisConsoleRotorStatePayload payload) {
        ServerPlayNetworking.send(player, payload);
    }

    @Override
    public void broadcastTardisConsoleRotorState(MinecraftServer server, TardisConsoleRotorStatePayload payload) {
        for (ServerPlayer player : PlayerLookup.all(server)) {
            sendTardisConsoleRotorState(player, payload);
        }
    }
}
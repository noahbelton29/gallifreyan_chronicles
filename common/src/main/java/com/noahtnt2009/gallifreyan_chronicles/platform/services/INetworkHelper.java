package com.noahtnt2009.gallifreyan_chronicles.platform.services;

import com.noahtnt2009.gallifreyan_chronicles.network.DimensionSkySyncPayload;
import com.noahtnt2009.gallifreyan_chronicles.network.TardisConsoleAnimationPayload;
import com.noahtnt2009.gallifreyan_chronicles.network.TardisConsoleRotorStatePayload;
import com.noahtnt2009.gallifreyan_chronicles.network.TardisConsoleSyncPayload;
import com.noahtnt2009.gallifreyan_chronicles.network.TardisExteriorSyncPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public interface INetworkHelper {
    void sendTardisExteriorSync(ServerPlayer player, TardisExteriorSyncPayload payload);
    void broadcastTardisExteriorSync(MinecraftServer server, TardisExteriorSyncPayload payload);
    void sendDimensionSkySync(ServerPlayer player, DimensionSkySyncPayload payload);
    void broadcastDimensionSkySync(MinecraftServer server, DimensionSkySyncPayload payload);
    void sendTardisConsoleSync(ServerPlayer player, TardisConsoleSyncPayload payload);
    void broadcastTardisConsoleSync(MinecraftServer server, TardisConsoleSyncPayload payload);
    void sendTardisConsoleAnimation(ServerPlayer player, TardisConsoleAnimationPayload payload);
    void broadcastTardisConsoleAnimation(MinecraftServer server, TardisConsoleAnimationPayload payload);

    void broadcastTardisConsoleAnimation(MinecraftServer server, TardisConsoleAnimationPayload payload, net.minecraft.server.level.ServerPlayer excludedPlayer);
    void sendTardisConsoleRotorState(ServerPlayer player, TardisConsoleRotorStatePayload payload);
    void broadcastTardisConsoleRotorState(MinecraftServer server, TardisConsoleRotorStatePayload payload);
}
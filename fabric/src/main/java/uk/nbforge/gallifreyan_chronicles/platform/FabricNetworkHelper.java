package uk.nbforge.gallifreyan_chronicles.platform;

import uk.nbforge.gallifreyan_chronicles.network.tardis.RematCatchPayload;
import uk.nbforge.gallifreyan_chronicles.network.ResumeSoundPayload;
import uk.nbforge.gallifreyan_chronicles.network.DimensionSkySyncPayload;
import uk.nbforge.gallifreyan_chronicles.network.tardis.TardisConsoleAnimationPayload;
import uk.nbforge.gallifreyan_chronicles.network.tardis.TardisConsoleRotorStatePayload;
import uk.nbforge.gallifreyan_chronicles.network.tardis.TardisConsoleSyncPayload;
import uk.nbforge.gallifreyan_chronicles.network.tardis.TardisDrumShakePayload;
import uk.nbforge.gallifreyan_chronicles.network.tardis.TardisAlarmPayload;
import uk.nbforge.gallifreyan_chronicles.network.tardis.TardisFrightPayload;
import uk.nbforge.gallifreyan_chronicles.network.DiscoveryToastPayload;
import uk.nbforge.gallifreyan_chronicles.network.tardis.TardisCrashRumblePayload;
import uk.nbforge.gallifreyan_chronicles.network.tardis.TardisExteriorSyncPayload;
import uk.nbforge.gallifreyan_chronicles.network.tardis.TardisInteriorSyncPayload;
import uk.nbforge.gallifreyan_chronicles.platform.services.INetworkHelper;
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
    public void sendTardisInteriorSync(ServerPlayer player, TardisInteriorSyncPayload payload) {
        ServerPlayNetworking.send(player, payload);
    }

    @Override
    public void broadcastTardisInteriorSync(MinecraftServer server, TardisInteriorSyncPayload payload) {
        for (ServerPlayer player : PlayerLookup.all(server)) {
            sendTardisInteriorSync(player, payload);
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

    @Override
    public void sendTardisDrumShake(ServerPlayer player, TardisDrumShakePayload payload) {
        ServerPlayNetworking.send(player, payload);
    }

    @Override
    public void sendTardisAlarm(ServerPlayer player, TardisAlarmPayload payload) {
        ServerPlayNetworking.send(player, payload);
    }

    @Override
    public void sendTardisFright(ServerPlayer player, TardisFrightPayload payload) {
        ServerPlayNetworking.send(player, payload);
    }

    @Override
    public void sendDiscoveryToast(ServerPlayer player, DiscoveryToastPayload payload) {
        ServerPlayNetworking.send(player, payload);
    }

    @Override
    public void sendTardisCrashRumble(ServerPlayer player, TardisCrashRumblePayload payload) {
        ServerPlayNetworking.send(player, payload);
    }

    @Override
    public void sendRematCatch(ServerPlayer player, RematCatchPayload payload) {
        ServerPlayNetworking.send(player, payload);
    }

    @Override
    public void sendResumeSound(ServerPlayer player, ResumeSoundPayload payload) {
        ServerPlayNetworking.send(player, payload);
    }
}
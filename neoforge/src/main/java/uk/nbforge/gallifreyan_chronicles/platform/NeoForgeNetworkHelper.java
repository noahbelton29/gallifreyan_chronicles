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
    public void sendTardisInteriorSync(ServerPlayer player, TardisInteriorSyncPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    @Override
    public void broadcastTardisInteriorSync(MinecraftServer server, TardisInteriorSyncPayload payload) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            sendTardisInteriorSync(player, payload);
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

    @Override
    public void sendTardisDrumShake(ServerPlayer player, TardisDrumShakePayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    @Override
    public void sendTardisAlarm(ServerPlayer player, TardisAlarmPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    @Override
    public void sendTardisFright(ServerPlayer player, TardisFrightPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    @Override
    public void sendDiscoveryToast(ServerPlayer player, DiscoveryToastPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    @Override
    public void sendTardisCrashRumble(ServerPlayer player, TardisCrashRumblePayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    @Override
    public void sendRematCatch(ServerPlayer player, RematCatchPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    @Override
    public void sendResumeSound(ServerPlayer player, ResumeSoundPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }
}
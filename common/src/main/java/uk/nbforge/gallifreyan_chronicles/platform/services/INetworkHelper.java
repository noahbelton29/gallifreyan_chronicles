package uk.nbforge.gallifreyan_chronicles.platform.services;

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
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public interface INetworkHelper {
    void sendTardisExteriorSync(ServerPlayer player, TardisExteriorSyncPayload payload);
    void broadcastTardisExteriorSync(MinecraftServer server, TardisExteriorSyncPayload payload);
    void sendTardisInteriorSync(ServerPlayer player, TardisInteriorSyncPayload payload);
    void broadcastTardisInteriorSync(MinecraftServer server, TardisInteriorSyncPayload payload);
    void sendDimensionSkySync(ServerPlayer player, DimensionSkySyncPayload payload);
    void broadcastDimensionSkySync(MinecraftServer server, DimensionSkySyncPayload payload);
    void sendTardisConsoleSync(ServerPlayer player, TardisConsoleSyncPayload payload);
    void broadcastTardisConsoleSync(MinecraftServer server, TardisConsoleSyncPayload payload);
    void sendTardisConsoleAnimation(ServerPlayer player, TardisConsoleAnimationPayload payload);
    void broadcastTardisConsoleAnimation(MinecraftServer server, TardisConsoleAnimationPayload payload);

    void broadcastTardisConsoleAnimation(MinecraftServer server, TardisConsoleAnimationPayload payload, ServerPlayer excludedPlayer);
    void sendTardisConsoleRotorState(ServerPlayer player, TardisConsoleRotorStatePayload payload);
    void broadcastTardisConsoleRotorState(MinecraftServer server, TardisConsoleRotorStatePayload payload);
    void sendTardisDrumShake(ServerPlayer player, TardisDrumShakePayload payload);
    void sendTardisAlarm(ServerPlayer player, TardisAlarmPayload payload);
    void sendTardisFright(ServerPlayer player, TardisFrightPayload payload);
    void sendDiscoveryToast(ServerPlayer player, DiscoveryToastPayload payload);
    void sendTardisCrashRumble(ServerPlayer player, TardisCrashRumblePayload payload);
    void sendRematCatch(ServerPlayer player, RematCatchPayload payload);
    void sendResumeSound(ServerPlayer player, ResumeSoundPayload payload);
}
package uk.nbforge.gallifreyan_chronicles.network;

import uk.nbforge.gallifreyan_chronicles.network.DiscoveryToastPayload;
import uk.nbforge.gallifreyan_chronicles.network.tardis.TardisCrashRumblePayload;
import uk.nbforge.gallifreyan_chronicles.network.tardis.RematCatchPayload;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityLevelChangeEvents;
import uk.nbforge.gallifreyan_chronicles.core.sound.timed.TimedSounds;
import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.loader.GCJsonReloadListener;
import uk.nbforge.gallifreyan_chronicles.core.lifecycle.ServerLifecycleHandler;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import uk.nbforge.gallifreyan_chronicles.compat.flashback.ReplayComponentIds;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import uk.nbforge.gallifreyan_chronicles.network.tardis.TardisAlarmPayload;
import uk.nbforge.gallifreyan_chronicles.network.tardis.TardisConsoleAnimationPayload;
import uk.nbforge.gallifreyan_chronicles.network.tardis.TardisConsoleRotorStatePayload;
import uk.nbforge.gallifreyan_chronicles.network.tardis.TardisConsoleSyncPayload;
import uk.nbforge.gallifreyan_chronicles.network.tardis.TardisDrumShakePayload;
import uk.nbforge.gallifreyan_chronicles.network.tardis.TardisExteriorSyncPayload;
import uk.nbforge.gallifreyan_chronicles.network.tardis.TardisFrightPayload;
import uk.nbforge.gallifreyan_chronicles.network.tardis.TardisInteriorSyncPayload;
import uk.nbforge.gallifreyan_chronicles.codec.network.DimensionSkySyncPayloadCodec;
import uk.nbforge.gallifreyan_chronicles.codec.network.DiscoveryToastPayloadCodec;
import uk.nbforge.gallifreyan_chronicles.codec.network.ResumeSoundPayloadCodec;
import uk.nbforge.gallifreyan_chronicles.codec.compat.flashback.ReplayComponentIdsPayloadCodec;
import uk.nbforge.gallifreyan_chronicles.tardis.codec.network.TardisPayloadCodec;

public final class GCFabricNetworking {
    private GCFabricNetworking() {
    }

    public static void registerNetworking() {
        PayloadTypeRegistry.clientboundPlay().register(TardisExteriorSyncPayload.TYPE, TardisPayloadCodec.TARDIS_EXTERIOR_SYNC);
        PayloadTypeRegistry.clientboundPlay().register(TardisInteriorSyncPayload.TYPE, TardisPayloadCodec.TARDIS_INTERIOR_SYNC);
        PayloadTypeRegistry.clientboundPlay().register(TardisConsoleSyncPayload.TYPE, TardisPayloadCodec.TARDIS_CONSOLE_SYNC);
        PayloadTypeRegistry.clientboundPlay().register(DimensionSkySyncPayload.TYPE, DimensionSkySyncPayloadCodec.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(TardisConsoleAnimationPayload.TYPE, TardisPayloadCodec.TARDIS_CONSOLE_ANIMATION);
        PayloadTypeRegistry.clientboundPlay().register(TardisConsoleRotorStatePayload.TYPE, TardisPayloadCodec.TARDIS_CONSOLE_ROTOR_STATE);
        PayloadTypeRegistry.clientboundPlay().register(TardisDrumShakePayload.TYPE, TardisPayloadCodec.TARDIS_DRUM_SHAKE);
        PayloadTypeRegistry.clientboundPlay().register(TardisAlarmPayload.TYPE, TardisPayloadCodec.TARDIS_ALARM);
        PayloadTypeRegistry.clientboundPlay().register(TardisFrightPayload.TYPE, TardisPayloadCodec.TARDIS_FRIGHT);
        PayloadTypeRegistry.clientboundPlay().register(DiscoveryToastPayload.TYPE, DiscoveryToastPayloadCodec.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(TardisCrashRumblePayload.TYPE, TardisPayloadCodec.TARDIS_CRASH_RUMBLE);
        PayloadTypeRegistry.clientboundPlay().register(RematCatchPayload.TYPE, TardisPayloadCodec.REMAT_CATCH);
        PayloadTypeRegistry.clientboundPlay().register(ResumeSoundPayload.TYPE, ResumeSoundPayloadCodec.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ReplayComponentIds.Payload.TYPE, ReplayComponentIdsPayloadCodec.STREAM_CODEC);

        ServerLifecycleEvents.SERVER_STARTED.register(GCJsonReloadListener::setCurrentServer);
        ServerLifecycleEvents.SERVER_STOPPED.register(_ -> ServerLifecycleHandler.onServerStop());

        ServerPlayConnectionEvents.JOIN.register((handler, sender, _) -> {
            sender.sendPacket(TardisExteriorSyncPayload.create());
            sender.sendPacket(TardisInteriorSyncPayload.create());
            sender.sendPacket(TardisConsoleSyncPayload.create());
            sender.sendPacket(DimensionSkySyncPayload.create());
            TimedSounds.resume(handler.getPlayer());
        });

        ServerEntityLevelChangeEvents.AFTER_PLAYER_CHANGE_LEVEL.register((player, _, _) -> TimedSounds.resume(player));

        Constants.LOG.info("Registered GC Networking (Fabric)");
    }
}
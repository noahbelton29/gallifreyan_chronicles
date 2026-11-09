package com.noahtnt2009.gallifreyan_chronicles.network;

import com.noahtnt2009.gallifreyan_chronicles.network.RematCatchPayload;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityLevelChangeEvents;
import com.noahtnt2009.gallifreyan_chronicles.sound.TimedSounds;
import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.data.GCJsonReloadListener;
import com.noahtnt2009.gallifreyan_chronicles.util.GCServerState;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import com.noahtnt2009.gallifreyan_chronicles.compat.flashback.ReplayComponentIds;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public final class GCFabricNetworking {
    private GCFabricNetworking() {
    }

    public static void registerNetworking() {
        PayloadTypeRegistry.clientboundPlay().register(TardisExteriorSyncPayload.TYPE, TardisExteriorSyncPayload.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(TardisInteriorSyncPayload.TYPE, TardisInteriorSyncPayload.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(TardisConsoleSyncPayload.TYPE, TardisConsoleSyncPayload.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(DimensionSkySyncPayload.TYPE, DimensionSkySyncPayload.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(TardisConsoleAnimationPayload.TYPE, TardisConsoleAnimationPayload.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(TardisConsoleRotorStatePayload.TYPE, TardisConsoleRotorStatePayload.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(TardisDrumShakePayload.TYPE, TardisDrumShakePayload.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(RematCatchPayload.TYPE, RematCatchPayload.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ResumeSoundPayload.TYPE, ResumeSoundPayload.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ReplayComponentIds.Payload.TYPE, ReplayComponentIds.Payload.STREAM_CODEC);

        ServerLifecycleEvents.SERVER_STARTED.register(GCJsonReloadListener::setCurrentServer);
        ServerLifecycleEvents.SERVER_STOPPED.register(_ -> GCServerState.onServerStop());

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
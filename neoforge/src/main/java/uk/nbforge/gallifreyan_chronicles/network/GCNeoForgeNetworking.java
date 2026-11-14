package uk.nbforge.gallifreyan_chronicles.network;

import uk.nbforge.gallifreyan_chronicles.network.DiscoveryToastPayload;
import uk.nbforge.gallifreyan_chronicles.network.tardis.TardisCrashRumblePayload;
import uk.nbforge.gallifreyan_chronicles.network.tardis.RematCatchPayload;
import uk.nbforge.gallifreyan_chronicles.core.sound.timed.TimedSounds;
import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.loader.GCJsonReloadListener;
import uk.nbforge.gallifreyan_chronicles.platform.Services;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import uk.nbforge.gallifreyan_chronicles.core.lifecycle.ServerLifecycleHandler;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
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
import uk.nbforge.gallifreyan_chronicles.tardis.codec.network.TardisPayloadCodec;

@EventBusSubscriber(modid = Constants.MOD_ID)
public final class GCNeoForgeNetworking {
    private GCNeoForgeNetworking() {
    }

    @SubscribeEvent
    public static void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(Constants.MOD_ID);
        registrar.playToClient(
                TardisExteriorSyncPayload.TYPE,
                TardisPayloadCodec.TARDIS_EXTERIOR_SYNC,
                (payload, context) -> context.enqueueWork(payload::apply)
        );
        registrar.playToClient(
                TardisInteriorSyncPayload.TYPE,
                TardisPayloadCodec.TARDIS_INTERIOR_SYNC,
                (payload, context) -> context.enqueueWork(payload::apply)
        );
        registrar.playToClient(
                DimensionSkySyncPayload.TYPE,
                DimensionSkySyncPayloadCodec.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(payload::apply)
        );
        registrar.playToClient(
                TardisConsoleSyncPayload.TYPE,
                TardisPayloadCodec.TARDIS_CONSOLE_SYNC,
                (payload, context) -> context.enqueueWork(payload::apply)
        );
        registrar.playToClient(
                TardisConsoleAnimationPayload.TYPE,
                TardisPayloadCodec.TARDIS_CONSOLE_ANIMATION,
                (payload, context) -> context.enqueueWork(payload::apply)
        );
        registrar.playToClient(
                TardisConsoleRotorStatePayload.TYPE,
                TardisPayloadCodec.TARDIS_CONSOLE_ROTOR_STATE,
                (payload, context) -> context.enqueueWork(payload::apply)
        );
        registrar.playToClient(
                TardisDrumShakePayload.TYPE,
                TardisPayloadCodec.TARDIS_DRUM_SHAKE,
                (payload, context) -> context.enqueueWork(payload::apply)
        );
        registrar.playToClient(
                TardisAlarmPayload.TYPE,
                TardisPayloadCodec.TARDIS_ALARM,
                (payload, context) -> context.enqueueWork(payload::apply)
        );
        registrar.playToClient(
                TardisFrightPayload.TYPE,
                TardisPayloadCodec.TARDIS_FRIGHT,
                (payload, context) -> context.enqueueWork(payload::apply)
        );
        registrar.playToClient(
                DiscoveryToastPayload.TYPE,
                DiscoveryToastPayloadCodec.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(payload::apply)
        );
        registrar.playToClient(
                TardisCrashRumblePayload.TYPE,
                TardisPayloadCodec.TARDIS_CRASH_RUMBLE,
                (payload, context) -> context.enqueueWork(payload::apply)
        );
        registrar.playToClient(
                RematCatchPayload.TYPE,
                TardisPayloadCodec.REMAT_CATCH,
                (payload, context) -> context.enqueueWork(payload::apply)
        );
        registrar.playToClient(
                ResumeSoundPayload.TYPE,
                ResumeSoundPayloadCodec.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(payload::apply)
        );
        Constants.LOG.info("Registered GC Networking");
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        GCJsonReloadListener.setCurrentServer(event.getServer());
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        ServerLifecycleHandler.onServerStop();
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        Services.NETWORK.sendTardisExteriorSync(player, TardisExteriorSyncPayload.create());
        Services.NETWORK.sendTardisInteriorSync(player, TardisInteriorSyncPayload.create());
        Services.NETWORK.sendTardisConsoleSync(player, TardisConsoleSyncPayload.create());
        Services.NETWORK.sendDimensionSkySync(player, DimensionSkySyncPayload.create());
        TimedSounds.resume(player);
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        TimedSounds.resume(player);
    }
}
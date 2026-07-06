package com.noahtnt2009.gallifreyan_chronicles.network;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.data.GCJsonReloadListener;
import com.noahtnt2009.gallifreyan_chronicles.platform.Services;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = Constants.MOD_ID)
public final class GCNeoForgeNetworking {
    private GCNeoForgeNetworking() {
    }

    @SubscribeEvent
    public static void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(Constants.MOD_ID);
        registrar.playToClient(
                TardisExteriorSyncPayload.TYPE,
                TardisExteriorSyncPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(payload::apply)
        );
        registrar.playToClient(
                DimensionSkySyncPayload.TYPE,
                DimensionSkySyncPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(payload::apply)
        );
        Constants.LOG.info("Registered GC Networking");
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        GCJsonReloadListener.setCurrentServer(event.getServer());
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        Services.NETWORK.sendTardisExteriorSync(player, TardisExteriorSyncPayload.create());
        Services.NETWORK.sendDimensionSkySync(player, DimensionSkySyncPayload.create());
    }
}
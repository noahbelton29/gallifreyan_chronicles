package com.noahtnt2009.gallifreyan_chronicles.event_subscriber;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.TardisExteriorLoader;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;

@EventBusSubscriber(modid = Constants.MOD_ID)
public class NeoForgeTardisExteriorLoader {
    public static final Identifier ID =
            Identifier.fromNamespaceAndPath(Constants.MOD_ID, "tardis_exteriors");

    @SubscribeEvent
    public static void onAddReloadListeners(AddServerReloadListenersEvent event) {
        // NeoForge doesn't hand this event a MinecraftServer reference; TardisExteriorLoader.currentServer
        // is (re)captured in GCNeoForgeNetworking on player join instead, which covers both the initial
        // load-before-any-players case (nothing to sync to yet) and every subsequent /reload done while
        // players are connected.
        event.addListener(ID, new TardisExteriorLoader());
    }
}
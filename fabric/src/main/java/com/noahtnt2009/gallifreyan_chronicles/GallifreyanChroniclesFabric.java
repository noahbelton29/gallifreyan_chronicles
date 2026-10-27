package com.noahtnt2009.gallifreyan_chronicles;

import com.noahtnt2009.gallifreyan_chronicles.init.*;
import com.noahtnt2009.gallifreyan_chronicles.network.GCFabricNetworking;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.ItemComponentTooltipProviderRegistry;

public class GallifreyanChroniclesFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        GCFabricCommands.registerCommands();
        GCEntityTypes.registerEntityTypes();
        GCBlockEntities.registerBlockEntities();
        GCEvents.registerEvents();
        GCCreativeModeTabs.registerFabricCreativeModeTabs();
        GCFabricWorldGeneration.registerWorldGeneration();
        GCFabricNetworking.registerNetworking();
        GCResourceLoaders.registerResourceLoaders();
        CommonClass.init();
        ItemComponentTooltipProviderRegistry.addLast(GCDataComponents.ITEM_INFO);
    }
}
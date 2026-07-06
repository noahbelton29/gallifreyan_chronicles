package com.noahtnt2009.gallifreyan_chronicles;

import com.noahtnt2009.gallifreyan_chronicles.init.*;
import com.noahtnt2009.gallifreyan_chronicles.network.GCFabricNetworking;
import net.fabricmc.api.ModInitializer;

public class GallifreyanChroniclesFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Constants.LOG.info("Hello Fabric world!");
        GCCommands.registerCommands();
        GCBlockEntities.registerBlockEntities();
        GCEvents.registerEvents();
        GCCreativeModeTabs.registerFabricCreativeModeTabs();
        GCFabricWorldGeneration.registerWorldGeneration();
        GCFabricNetworking.registerNetworking();
        GCResourceLoaders.registerResourceLoaders();
        CommonClass.init();
    }
}
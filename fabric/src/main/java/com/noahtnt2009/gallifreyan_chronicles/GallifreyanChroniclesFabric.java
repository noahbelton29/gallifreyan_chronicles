package com.noahtnt2009.gallifreyan_chronicles;

import com.noahtnt2009.gallifreyan_chronicles.init.*;
import com.noahtnt2009.gallifreyan_chronicles.loader.GCFabricDimensionSkyLoader;
import com.noahtnt2009.gallifreyan_chronicles.loader.GCFabricTardisExteriorLoader;
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
        GCFabricTardisExteriorLoader.registerTardisExteriorLoader();
        GCFabricDimensionSkyLoader.registerDimensionSkyLoader();
        CommonClass.init();
    }
}
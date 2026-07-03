package com.noahtnt2009.gallifreyan_chronicles;

import com.noahtnt2009.gallifreyan_chronicles.init.*;
import com.noahtnt2009.gallifreyan_chronicles.loader.GCFabricTardisExteriorLoader;
import com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.TardisExteriorLoader;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.server.packs.PackType;

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
        CommonClass.init();
    }
}

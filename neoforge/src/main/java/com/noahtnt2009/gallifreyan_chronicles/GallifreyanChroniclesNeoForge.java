package com.noahtnt2009.gallifreyan_chronicles;

import com.noahtnt2009.gallifreyan_chronicles.init.GCBlockEntities;
import com.noahtnt2009.gallifreyan_chronicles.init.GCCreativeModeTabs;
import com.noahtnt2009.gallifreyan_chronicles.init.GCEntityTypes;
import com.noahtnt2009.gallifreyan_chronicles.init.GCEvents;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

@Mod(Constants.MOD_ID)
public class GallifreyanChroniclesNeoForge {
    public GallifreyanChroniclesNeoForge(IEventBus eventBus) {
        GCEntityTypes.registerEntityTypes(eventBus);
        GCBlockEntities.registerBlockEntities(eventBus);
        GCEvents.registerEvents();
        GCCreativeModeTabs.registerNeoForgeCreativeModeTabs(eventBus);
        eventBus.addListener(this::onRegister);
        eventBus.addListener(this::onClientSetup);
    }

    private void onRegister(RegisterEvent event) {
        CommonClass.init();
    }

    public void onClientSetup(FMLClientSetupEvent event) {}
}

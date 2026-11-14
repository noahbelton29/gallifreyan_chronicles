package uk.nbforge.gallifreyan_chronicles;

import uk.nbforge.gallifreyan_chronicles.core.GCBlockEntities;
import uk.nbforge.gallifreyan_chronicles.core.GCCreativeModeTabs;
import uk.nbforge.gallifreyan_chronicles.core.GCEntityTypes;
import uk.nbforge.gallifreyan_chronicles.core.GCEvents;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import uk.nbforge.gallifreyan_chronicles.loaders.GCNeoForgeResourceLoaders;

@Mod(Constants.MOD_ID)
public class GallifreyanChroniclesNeoForge {
    public GallifreyanChroniclesNeoForge(IEventBus eventBus) {
        GCEntityTypes.registerEntityTypes(eventBus);
        GCBlockEntities.registerBlockEntities(eventBus);
        GCEvents.registerEvents();
        GCCreativeModeTabs.registerNeoForgeCreativeModeTabs(eventBus);
        GCNeoForgeResourceLoaders.registerResourceLoaders();
        eventBus.addListener(this::onRegister);
        eventBus.addListener(this::onClientSetup);
    }

    private void onRegister(RegisterEvent event) {
        CommonClass.init();
    }

    public void onClientSetup(FMLClientSetupEvent event) {
    }
}

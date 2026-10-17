package com.noahtnt2009.gallifreyan_chronicles;

import com.noahtnt2009.gallifreyan_chronicles.datagen.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = Constants.MOD_ID)
public class GallifreyanChroniclesDataGenerators {

    @SubscribeEvent
    public static void gatherClientData(GatherDataEvent.Client event) {
        PackOutput packOutput = event.getGenerator().getPackOutput();

        CompletableFuture<HolderLookup.Provider> worldLookup =
                event.getWorldLookupProvider();
        CompletableFuture<HolderLookup.Provider> reloadableLookup =
                event.getReloadableLookupProvider();

        event.addProvider(new GCModelProvider(packOutput));
        event.addProvider(new GCLanguageProvider(packOutput));
        event.addProvider(GCModpackDataProvider.create(packOutput, worldLookup));
        event.addProvider(GCModpackDataProvider.createReloadable(packOutput, worldLookup, reloadableLookup));
        event.addProvider(new GCBlockTagProvider(packOutput, reloadableLookup));
        event.addProvider(new GCItemTagProvider(packOutput, reloadableLookup));
        event.addProvider(new GCEquipmentAssetProvider(packOutput));
        event.addProvider(new GCDimensionSkyProvider(packOutput));
    }
}
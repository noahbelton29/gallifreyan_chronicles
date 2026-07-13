package com.noahtnt2009.gallifreyan_chronicles;

import com.noahtnt2009.gallifreyan_chronicles.datagen.*;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.Collections;
import java.util.List;

@EventBusSubscriber(modid = Constants.MOD_ID)
public class GallifreyanChroniclesDataGenerators {

    @SubscribeEvent
    public static void gatherClientData(GatherDataEvent.Client event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        var lookupProvider = event.getLookupProvider();

        generator.addProvider(true, new GCModelProvider(packOutput));
        generator.addProvider(true, new GCLanguageProvider(packOutput));
        generator.addProvider(true, new GCModpackDataProvider(packOutput, lookupProvider));
        generator.addProvider(true, new GCBlockTagProvider(packOutput, lookupProvider));
        generator.addProvider(true, new GCItemTagProvider(packOutput, lookupProvider));
        generator.addProvider(true, new LootTableProvider(packOutput, Collections.emptySet(),
                List.of(new LootTableProvider.SubProviderEntry(GCBlockLootTableProvider::new, LootContextParamSets.BLOCK)
                ), lookupProvider));
        generator.addProvider(true, new GCEquipmentAssetProvider(packOutput));
        generator.addProvider(true, new GCRecipeProvider.Runner(packOutput, lookupProvider));
        generator.addProvider(true, new GCDimensionSkyProvider(packOutput));
    }
}
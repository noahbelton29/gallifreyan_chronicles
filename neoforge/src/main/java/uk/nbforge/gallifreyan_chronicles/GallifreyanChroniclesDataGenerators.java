package uk.nbforge.gallifreyan_chronicles;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;
import uk.nbforge.gallifreyan_chronicles.datagen.assets.GCModelProvider;
import uk.nbforge.gallifreyan_chronicles.datagen.assets.GCEquipmentAssetProvider;
import uk.nbforge.gallifreyan_chronicles.datagen.assets.GCSoundsProvider;
import uk.nbforge.gallifreyan_chronicles.datagen.assets.lang.GCLanguageProvider;
import uk.nbforge.gallifreyan_chronicles.datagen.assets.lang.GCSpanishLanguageProvider;
import uk.nbforge.gallifreyan_chronicles.datagen.data.GCModpackDataProvider;
import uk.nbforge.gallifreyan_chronicles.datagen.data.GCDimensionSkyProvider;
import uk.nbforge.gallifreyan_chronicles.datagen.data.tag.GCBlockTagProvider;
import uk.nbforge.gallifreyan_chronicles.datagen.data.tag.GCItemTagProvider;
import uk.nbforge.gallifreyan_chronicles.datagen.tardis.GCTardisConsoleProvider;
import uk.nbforge.gallifreyan_chronicles.datagen.tardis.GCTardisConsoleVariantProvider;
import uk.nbforge.gallifreyan_chronicles.datagen.tardis.GCTardisDoorVariantProvider;
import uk.nbforge.gallifreyan_chronicles.datagen.tardis.GCTardisExteriorProvider;
import uk.nbforge.gallifreyan_chronicles.datagen.tardis.GCTardisFadeProvider;
import uk.nbforge.gallifreyan_chronicles.datagen.tardis.GCTardisInteriorProvider;
import uk.nbforge.gallifreyan_chronicles.datagen.tardis.GCTardisPersonalityProvider;
import uk.nbforge.gallifreyan_chronicles.datagen.tardis.GCTardisTraitProvider;

@EventBusSubscriber(modid = Constants.MOD_ID)
public class GallifreyanChroniclesDataGenerators {

    @SubscribeEvent
    public static void gatherClientData(GatherDataEvent.Client event) {
        PackOutput packOutput = event.getGenerator().getPackOutput();

        CompletableFuture<HolderLookup.Provider> worldLookup =
                event.getWorldLookupProvider();
        CompletableFuture<HolderLookup.Provider> reloadableLookup =
                event.getReloadableLookupProvider();

        event.addProvider(GCModpackDataProvider.create(packOutput, worldLookup));
        event.addProvider(GCModpackDataProvider.createReloadable(packOutput, worldLookup, reloadableLookup));
        event.addProvider(new GCModelProvider(packOutput));
        event.addProvider(new GCLanguageProvider(packOutput));
        event.addProvider(new GCSpanishLanguageProvider(packOutput));
        event.addProvider(new GCBlockTagProvider(packOutput, reloadableLookup));
        event.addProvider(new GCItemTagProvider(packOutput, reloadableLookup));
        event.addProvider(new GCEquipmentAssetProvider(packOutput));
        event.addProvider(new GCDimensionSkyProvider(packOutput));
        event.addProvider(new GCSoundsProvider(packOutput));
        event.addProvider(new GCTardisFadeProvider(packOutput));
        event.addProvider(new GCTardisExteriorProvider(packOutput));
        event.addProvider(new GCTardisInteriorProvider(packOutput));
        event.addProvider(new GCTardisConsoleProvider(packOutput));
        event.addProvider(new GCTardisPersonalityProvider(packOutput));
        event.addProvider(new GCTardisTraitProvider(packOutput));
        event.addProvider(new GCTardisDoorVariantProvider(packOutput));
        event.addProvider(new GCTardisConsoleVariantProvider(packOutput));
    }
}
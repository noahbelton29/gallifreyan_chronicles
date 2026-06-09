package com.noahtnt2009.gallifreyan_chronicles.datagen;

import com.noahtnt2009.gallifreyan_chronicles.registry.GCBlocks;
import com.noahtnt2009.gallifreyan_chronicles.registry.GCCreativeModeTabs;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class GCLanguageProvider extends FabricLanguageProvider {
    public GCLanguageProvider(FabricPackOutput packOutput, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(packOutput, "en_us", registryLookup);
    }

    @Override
    public void generateTranslations(HolderLookup.Provider registryLookup, TranslationBuilder builder) {
        for (Block block : GCBlocks.BLOCKS) {
            String path = BuiltInRegistries.BLOCK.getKey(block).getPath();
            String displayName = Arrays.stream(path.split("_"))
                    .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1))
                    .collect(Collectors.joining(" "));
            builder.add(block, displayName);
        }

        builder.add(GCCreativeModeTabs.PLANET_ITEMS_TAB.getDisplayName().getString(), "GC: Planet Items");
    }
}

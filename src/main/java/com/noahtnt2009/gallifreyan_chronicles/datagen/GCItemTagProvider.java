package com.noahtnt2009.gallifreyan_chronicles.datagen;

import com.noahtnt2009.gallifreyan_chronicles.registry.GCBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.ItemTags;

import java.util.concurrent.CompletableFuture;

public class GCItemTagProvider extends FabricTagsProvider.ItemTagsProvider {
    public GCItemTagProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registryLookupFuture) {
        super(output, registryLookupFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        valueLookupBuilder(ItemTags.LOGS_THAT_BURN)
                .add(GCBlocks.CADONWOOD_LOG.asItem(),
                        GCBlocks.CADONWOOD_WOOD.asItem(),
                        GCBlocks.STRIPPED_CADONWOOD_LOG.asItem(),
                        GCBlocks.STRIPPED_CADONWOOD_WOOD.asItem());

        valueLookupBuilder(ItemTags.PLANKS)
                .add(GCBlocks.CADONWOOD_PLANKS.asItem());
    }
}

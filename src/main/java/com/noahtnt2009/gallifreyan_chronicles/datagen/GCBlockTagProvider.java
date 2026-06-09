package com.noahtnt2009.gallifreyan_chronicles.datagen;

import com.noahtnt2009.gallifreyan_chronicles.registry.GCBlocks;
import com.noahtnt2009.gallifreyan_chronicles.registry.GCTags;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.BlockTags;

import java.util.concurrent.CompletableFuture;

public class GCBlockTagProvider extends FabricTagsProvider.BlockTagsProvider {
    public GCBlockTagProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registryLookupFuture) {
        super(output, registryLookupFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        valueLookupBuilder(BlockTags.SAND)
                .add(GCBlocks.GALLIFREYAN_SAND);

        valueLookupBuilder(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(GCBlocks.GALLIFREYAN_COBBLESTONE,
                        GCBlocks.GALLIFREYAN_STONE,
                        GCBlocks.GALLIFREYAN_STONE_IRON_ORE,
                        GCBlocks.GALLIFREYAN_STONE_COAL_ORE);

        valueLookupBuilder(BlockTags.MINEABLE_WITH_SHOVEL)
                .add(GCBlocks.GALLIFREYAN_DIRT,
                        GCBlocks.GALLIFREYAN_GRASS_BLOCK,
                        GCBlocks.GALLIFREYAN_GRAVEL,
                        GCBlocks.GALLIFREYAN_SAND);

        valueLookupBuilder(BlockTags.MINEABLE_WITH_AXE)
                .add(GCBlocks.CADONWOOD_PLANKS,
                        GCBlocks.CADONWOOD_LOG,
                        GCBlocks.STRIPPED_CADONWOOD_LOG,
                        GCBlocks.CADONWOOD_WOOD,
                        GCBlocks.STRIPPED_CADONWOOD_WOOD);

        valueLookupBuilder(BlockTags.MINEABLE_WITH_HOE)
                .add(GCBlocks.CADONWOOD_LEAVES);

        valueLookupBuilder(BlockTags.NEEDS_STONE_TOOL)
                .add(GCBlocks.GALLIFREYAN_STONE_IRON_ORE,
                        GCBlocks.ARCADIAN_SHALE);

        valueLookupBuilder(BlockTags.ANIMALS_SPAWNABLE_ON)
                .add(GCBlocks.GALLIFREYAN_SAND).setReplace(false);

        valueLookupBuilder(BlockTags.REPLACEABLE)
                .add(GCBlocks.SHORT_GALLIFREYAN_DRY_GRASS)
                .add(GCBlocks.TALL_GALLIFREYAN_DRY_GRASS);

        valueLookupBuilder(BlockTags.GRASS_BLOCKS)
                .add(GCBlocks.GALLIFREYAN_GRASS_BLOCK)
                .add(GCBlocks.GALLIFREYAN_SAND);

        valueLookupBuilder(GCTags.Blocks.GALLIFREYAN_STONE_REPLACEABLES)
                .add(GCBlocks.GALLIFREYAN_STONE);

        valueLookupBuilder(GCTags.Blocks.BASE_STONE_GALLIFREY)
                .add(GCBlocks.GALLIFREYAN_STONE);

        valueLookupBuilder(BlockTags.LOGS_THAT_BURN)
                .add(GCBlocks.CADONWOOD_LOG,
                        GCBlocks.CADONWOOD_WOOD,
                        GCBlocks.STRIPPED_CADONWOOD_LOG,
                        GCBlocks.STRIPPED_CADONWOOD_WOOD);

        valueLookupBuilder(BlockTags.SAPLINGS)
                .add(GCBlocks.CADONWOOD_SAPLING);

        valueLookupBuilder(BlockTags.DIRT)
                .add(GCBlocks.GALLIFREYAN_GRASS_BLOCK)
                .add(GCBlocks.GALLIFREYAN_DIRT);

        valueLookupBuilder(BlockTags.LOGS)
                .add(GCBlocks.CADONWOOD_LOG)
                .add(GCBlocks.STRIPPED_CADONWOOD_LOG);
    }
}

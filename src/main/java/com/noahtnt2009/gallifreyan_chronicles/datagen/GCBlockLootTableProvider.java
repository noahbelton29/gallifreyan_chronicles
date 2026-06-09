package com.noahtnt2009.gallifreyan_chronicles.datagen;

import com.noahtnt2009.gallifreyan_chronicles.registry.GCBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootSubProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.Items;

import java.util.concurrent.CompletableFuture;

public class GCBlockLootTableProvider extends FabricBlockLootSubProvider {
    public GCBlockLootTableProvider(FabricPackOutput packOutput, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(packOutput, registriesFuture);
    }

    @Override
    public void generate() {
        dropSelf(GCBlocks.CADONWOOD_PLANKS);
        dropSelf(GCBlocks.GALLIFREYAN_DIRT);
        dropSelf(GCBlocks.CADONWOOD_WOOD);
        dropSelf(GCBlocks.STRIPPED_CADONWOOD_LOG);
        dropSelf(GCBlocks.STRIPPED_CADONWOOD_WOOD);
        dropSelf(GCBlocks.GALLIFREYAN_GRAVEL);
        dropSelf(GCBlocks.ARCADIAN_SHALE);
        dropSelf(GCBlocks.GALLIFREYAN_SAND);
        dropSelf(GCBlocks.GALLIFREYAN_MUD);
        dropSelf(GCBlocks.CADONWOOD_LOG);
        dropSelf(GCBlocks.CADONWOOD_SAPLING);
        dropSelf(GCBlocks.GALLIFREYAN_COBBLESTONE);

        add(GCBlocks.POTTED_CADONWOOD_SAPLING, createPotFlowerItemTable(GCBlocks.CADONWOOD_SAPLING));
        add(GCBlocks.GALLIFREYAN_STONE, createSingleItemTable(GCBlocks.GALLIFREYAN_COBBLESTONE));
        add(GCBlocks.GALLIFREYAN_GRASS_BLOCK, createSingleItemTable(GCBlocks.GALLIFREYAN_DIRT));
        add(GCBlocks.CADONWOOD_LEAVES, createLeavesDrops(GCBlocks.CADONWOOD_LEAVES, GCBlocks.CADONWOOD_SAPLING, NORMAL_LEAVES_SAPLING_CHANCES));

        add(GCBlocks.SHORT_GALLIFREYAN_GRASS, createGrassDrops(GCBlocks.SHORT_GALLIFREYAN_GRASS));
        add(GCBlocks.TALL_GALLIFREYAN_GRASS, createGrassDrops(GCBlocks.TALL_GALLIFREYAN_GRASS));

        add(GCBlocks.GALLIFREYAN_STONE_IRON_ORE, createOreDrop(GCBlocks.GALLIFREYAN_STONE_IRON_ORE, Items.RAW_IRON));
        add(GCBlocks.GALLIFREYAN_STONE_COAL_ORE, createOreDrop(GCBlocks.GALLIFREYAN_STONE_COAL_ORE, Items.COAL));
    }
}

package com.noahtnt2009.gallifreyan_chronicles.datagen;

import com.noahtnt2009.gallifreyan_chronicles.init.GCBlocks;
import com.noahtnt2009.gallifreyan_chronicles.init.GCItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.NonNull;

import java.util.Set;

public class GCBlockLootTableProvider extends BlockLootSubProvider {
    public GCBlockLootTableProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
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
        dropSelf(GCBlocks.GALLIFREYAN_VINE);
        dropSelf(GCBlocks.COBBLED_LIMESTONE);
        dropSelf(GCBlocks.THORIVINE);
        dropSelf(GCBlocks.THORIVINE_FLOWER);
        dropSelf(GCBlocks.GALLIFREYAN_MUDSTONE);
        dropSelf(GCBlocks.LUNAR_REGOLITH);
        dropSelf(GCBlocks.MOONSLATE);
        dropSelf(GCBlocks.MOON_DUST);
        dropSelf(GCBlocks.TARDIS);

        add(GCBlocks.POTTED_CADONWOOD_SAPLING, createPotFlowerItemTable(GCBlocks.CADONWOOD_SAPLING));
        add(GCBlocks.GALLIFREYAN_STONE, createSingleItemTable(GCBlocks.GALLIFREYAN_COBBLESTONE));
        add(GCBlocks.GALLIFREYAN_GRASS_BLOCK, createSingleItemTable(GCBlocks.GALLIFREYAN_DIRT));
        add(GCBlocks.CADONWOOD_LEAVES, createLeavesDrops(GCBlocks.CADONWOOD_LEAVES, GCBlocks.CADONWOOD_SAPLING, NORMAL_LEAVES_SAPLING_CHANCES));

        add(GCBlocks.SHORT_GALLIFREYAN_DRY_GRASS, createGrassDrops(GCBlocks.SHORT_GALLIFREYAN_DRY_GRASS));
        add(GCBlocks.SHORT_GALLIFREYAN_GRASS, createGrassDrops(GCBlocks.SHORT_GALLIFREYAN_GRASS));
        add(GCBlocks.TALL_GALLIFREYAN_GRASS, createGrassDrops(GCBlocks.TALL_GALLIFREYAN_GRASS));
        add(GCBlocks.TALL_GALLIFREYAN_DRY_GRASS, createGrassDrops(GCBlocks.TALL_GALLIFREYAN_DRY_GRASS));

        add(GCBlocks.GALLIFREYAN_STONE_IRON_ORE, createOreDrop(GCBlocks.GALLIFREYAN_STONE_IRON_ORE, Items.RAW_IRON));
        add(GCBlocks.GALLIFREYAN_STONE_COAL_ORE, createOreDrop(GCBlocks.GALLIFREYAN_STONE_COAL_ORE, Items.COAL));
        add(GCBlocks.ARCADIAN_SHALE_TARANIUM_ORE, createOreDrop(GCBlocks.ARCADIAN_SHALE_TARANIUM_ORE, GCItems.TARANIUM_SCRAP));

        dropSelf(GCBlocks.CADONWOOD_STAIRS);
        add(GCBlocks.CADONWOOD_SLAB, this::createSlabItemTable);

        dropSelf(GCBlocks.CADONWOOD_PRESSURE_PLATE);
        dropSelf(GCBlocks.CADONWOOD_BUTTON);

        dropSelf(GCBlocks.CADONWOOD_FENCE);
        dropSelf(GCBlocks.CADONWOOD_FENCE_GATE);
        dropSelf(GCBlocks.CADONWOOD_WALL);
    }

    @Override
    protected @NonNull Iterable<Block> getKnownBlocks() {
        return GCBlocks.BLOCKS;
    }
}
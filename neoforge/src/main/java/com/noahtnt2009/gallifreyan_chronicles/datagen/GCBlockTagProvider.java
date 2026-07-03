package com.noahtnt2009.gallifreyan_chronicles.datagen;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.init.GCBlocks;
import com.noahtnt2009.gallifreyan_chronicles.init.GCTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.tags.BlockItemTagId;
import net.minecraft.tags.BlockItemTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.BlockTagsProvider;

import java.util.concurrent.CompletableFuture;

public class GCBlockTagProvider extends BlockTagsProvider {
    public GCBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, Constants.MOD_ID);
    }

    private void tagAdd(TagKey<Block> tag, Block... blocks) {
        TagAppender<Block> appender = tag(tag);
        for (Block b : blocks) {
            appender.add(BuiltInRegistries.BLOCK.getResourceKey(b).orElseThrow());
        }
    }

    private TagAppender<Block> tagAppend(TagKey<Block> tag, Block... blocks) {
        TagAppender<Block> appender = tag(tag);
        for (Block b : blocks) {
            appender.add(BuiltInRegistries.BLOCK.getResourceKey(b).orElseThrow());
        }
        return appender;
    }

    private void tagAddBlockItem(BlockItemTagId pair, Block... blocks) {
        tagAdd(pair.block(), blocks);
        // item side is handled by ItemTagsProvider via copy()
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tagAdd(BlockTags.CLIMBABLE,
                GCBlocks.GALLIFREYAN_VINE);

        tagAdd(BlockTags.SAND,
                GCBlocks.GALLIFREYAN_SAND);

        tagAppend(GCTags.Blocks.NEEDS_NETHERITE_TOOL,
                GCBlocks.ARCADIAN_SHALE_TARANIUM_ORE);

        tagAppend(BlockTags.INCORRECT_FOR_DIAMOND_TOOL)
                .addTag(GCTags.Blocks.NEEDS_NETHERITE_TOOL);

        tagAdd(BlockTags.MINEABLE_WITH_PICKAXE,
                GCBlocks.GALLIFREYAN_COBBLESTONE,
                GCBlocks.GALLIFREYAN_STONE,
                GCBlocks.GALLIFREYAN_STONE_IRON_ORE,
                GCBlocks.GALLIFREYAN_STONE_COAL_ORE,
                GCBlocks.ARCADIAN_SHALE_TARANIUM_ORE,
                GCBlocks.ARCADIAN_SHALE,
                GCBlocks.GALLIFREYAN_MUDSTONE,
                GCBlocks.MOONSLATE,
                GCBlocks.LUNAR_REGOLITH);

        tagAdd(BlockTags.MINEABLE_WITH_SHOVEL,
                GCBlocks.GALLIFREYAN_DIRT,
                GCBlocks.GALLIFREYAN_GRASS_BLOCK,
                GCBlocks.GALLIFREYAN_GRAVEL,
                GCBlocks.GALLIFREYAN_SAND,
                GCBlocks.GALLIFREYAN_MUD,
                GCBlocks.MOON_DUST);

        tagAdd(BlockTags.MINEABLE_WITH_AXE,
                GCBlocks.CADONWOOD_PLANKS,
                GCBlocks.CADONWOOD_LOG,
                GCBlocks.STRIPPED_CADONWOOD_LOG,
                GCBlocks.CADONWOOD_WOOD,
                GCBlocks.STRIPPED_CADONWOOD_WOOD,
                GCBlocks.CADONWOOD_STAIRS,
                GCBlocks.CADONWOOD_FENCE,
                GCBlocks.CADONWOOD_FENCE_GATE,
                GCBlocks.CADONWOOD_WALL,
                GCBlocks.CADONWOOD_BUTTON,
                GCBlocks.CADONWOOD_PRESSURE_PLATE);

        tagAdd(BlockTags.MINEABLE_WITH_HOE,
                GCBlocks.CADONWOOD_LEAVES);

        tagAdd(BlockTags.NEEDS_STONE_TOOL,
                GCBlocks.GALLIFREYAN_STONE_IRON_ORE,
                GCBlocks.ARCADIAN_SHALE);

        tagAppend(BlockTags.ANIMALS_SPAWNABLE_ON,
                GCBlocks.GALLIFREYAN_SAND).replace(false);

        tagAppend(BlockTags.REPLACEABLE,
                GCBlocks.SHORT_GALLIFREYAN_DRY_GRASS,
                GCBlocks.TALL_GALLIFREYAN_DRY_GRASS);

        tagAppend(BlockTags.GRASS_BLOCKS,
                GCBlocks.GALLIFREYAN_GRASS_BLOCK,
                GCBlocks.GALLIFREYAN_SAND);

        tagAppend(GCTags.Blocks.GALLIFREYAN_STONE_REPLACEABLES,
                GCBlocks.GALLIFREYAN_STONE);

        tagAppend(GCTags.Blocks.BASE_STONE_GALLIFREY,
                GCBlocks.GALLIFREYAN_STONE);

        tagAddBlockItem(BlockItemTags.LOGS_THAT_BURN,
                GCBlocks.CADONWOOD_LOG,
                GCBlocks.CADONWOOD_WOOD,
                GCBlocks.STRIPPED_CADONWOOD_LOG,
                GCBlocks.STRIPPED_CADONWOOD_WOOD);

        tagAddBlockItem(BlockItemTags.SAPLINGS,
                GCBlocks.CADONWOOD_SAPLING);

        tagAdd(BlockTags.DIRT,
                GCBlocks.GALLIFREYAN_GRASS_BLOCK,
                GCBlocks.GALLIFREYAN_DIRT);

        tagAddBlockItem(BlockItemTags.LOGS,
                GCBlocks.CADONWOOD_LOG,
                GCBlocks.STRIPPED_CADONWOOD_LOG);

        tagAdd(BlockTags.STAIRS,
                GCBlocks.CADONWOOD_STAIRS);

        tagAdd(BlockTags.SLABS,
                GCBlocks.CADONWOOD_SLAB);

        tagAdd(BlockTags.PRESSURE_PLATES,
                GCBlocks.CADONWOOD_PRESSURE_PLATE);

        tagAdd(BlockTags.BUTTONS,
                GCBlocks.CADONWOOD_BUTTON);

        tagAdd(BlockTags.FENCES,
                GCBlocks.CADONWOOD_FENCE);

        tagAdd(BlockTags.FENCE_GATES,
                GCBlocks.CADONWOOD_FENCE_GATE);

        tagAdd(BlockTags.WALLS,
                GCBlocks.CADONWOOD_WALL);
    }
}
package uk.nbforge.gallifreyan_chronicles.datagen.data.tag;

import java.util.concurrent.CompletableFuture;
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
import org.jspecify.annotations.NonNull;
import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.core.GCBlocks;
import uk.nbforge.gallifreyan_chronicles.core.GCTags;
import uk.nbforge.gallifreyan_chronicles.core.block.registry.CadonwoodBlocks;
import uk.nbforge.gallifreyan_chronicles.core.block.registry.FloraBlocks;
import uk.nbforge.gallifreyan_chronicles.core.block.registry.GallifreyBlocks;
import uk.nbforge.gallifreyan_chronicles.core.block.registry.InteriorBlocks;
import uk.nbforge.gallifreyan_chronicles.core.block.registry.MoonBlocks;
import uk.nbforge.gallifreyan_chronicles.core.block.registry.RoundelBlocks;

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
    }

    @Override
    protected void addTags(HolderLookup.@NonNull Provider provider) {
        tagAdd(BlockTags.CLIMBABLE, FloraBlocks.GALLIFREYAN_VINE);

        tagAdd(BlockTags.SAND, GallifreyBlocks.GALLIFREYAN_SAND);

        tagAppend(GCTags.Blocks.NEEDS_NETHERITE_TOOL, GallifreyBlocks.ARCADIAN_SHALE_TARANIUM_ORE);

        tagAppend(BlockTags.INCORRECT_FOR_DIAMOND_TOOL).addTag(GCTags.Blocks.NEEDS_NETHERITE_TOOL);

        tagAdd(
                BlockTags.MINEABLE_WITH_PICKAXE,
                GallifreyBlocks.GALLIFREYAN_COBBLESTONE,
                GallifreyBlocks.GALLIFREYAN_STONE,
                GallifreyBlocks.GALLIFREYAN_STONE_IRON_ORE,
                GallifreyBlocks.GALLIFREYAN_STONE_COAL_ORE,
                GallifreyBlocks.ARCADIAN_SHALE_TARANIUM_ORE,
                GallifreyBlocks.ARCADIAN_SHALE,
                GallifreyBlocks.GALLIFREYAN_MUDSTONE,
                MoonBlocks.MOONSLATE,
                MoonBlocks.LUNAR_REGOLITH);
        tagAdd(BlockTags.MINEABLE_WITH_PICKAXE, GCBlocks.interiorDoorBlocks());

        tagAdd(
                BlockTags.MINEABLE_WITH_SHOVEL,
                GallifreyBlocks.GALLIFREYAN_DIRT,
                GallifreyBlocks.GALLIFREYAN_GRASS_BLOCK,
                GallifreyBlocks.GALLIFREYAN_GRAVEL,
                GallifreyBlocks.GALLIFREYAN_SAND,
                GallifreyBlocks.GALLIFREYAN_MUD,
                MoonBlocks.MOON_DUST);

        tagAdd(
                BlockTags.MINEABLE_WITH_AXE,
                CadonwoodBlocks.CADONWOOD_PLANKS,
                CadonwoodBlocks.CADONWOOD_LOG,
                CadonwoodBlocks.STRIPPED_CADONWOOD_LOG,
                CadonwoodBlocks.CADONWOOD_WOOD,
                CadonwoodBlocks.STRIPPED_CADONWOOD_WOOD,
                CadonwoodBlocks.CADONWOOD_STAIRS,
                CadonwoodBlocks.CADONWOOD_FENCE,
                CadonwoodBlocks.CADONWOOD_FENCE_GATE,
                CadonwoodBlocks.CADONWOOD_WALL,
                CadonwoodBlocks.CADONWOOD_BUTTON,
                CadonwoodBlocks.CADONWOOD_PRESSURE_PLATE);
        tagAdd(BlockTags.MINEABLE_WITH_AXE, RoundelBlocks.WOOD_ROUNDELS.values().toArray(Block[]::new));
        tagAdd(
                BlockTags.MINEABLE_WITH_AXE,
                RoundelBlocks.WOOD_HALF_ROUNDELS.values().toArray(Block[]::new));
        tagAdd(
                BlockTags.MINEABLE_WITH_PICKAXE,
                RoundelBlocks.CONCRETE_ROUNDELS.values().toArray(Block[]::new));
        tagAdd(
                BlockTags.MINEABLE_WITH_PICKAXE,
                RoundelBlocks.CONCRETE_HALF_ROUNDELS.values().toArray(Block[]::new));

        tagAdd(BlockTags.MINEABLE_WITH_HOE, CadonwoodBlocks.CADONWOOD_LEAVES);

        tagAdd(BlockTags.NEEDS_STONE_TOOL, GallifreyBlocks.GALLIFREYAN_STONE_IRON_ORE, GallifreyBlocks.ARCADIAN_SHALE);

        tagAppend(BlockTags.ANIMALS_SPAWNABLE_ON, GallifreyBlocks.GALLIFREYAN_SAND)
                .replace(false);

        tagAppend(
                BlockTags.REPLACEABLE, FloraBlocks.SHORT_GALLIFREYAN_DRY_GRASS, FloraBlocks.TALL_GALLIFREYAN_DRY_GRASS);

        tagAppend(BlockTags.GRASS_BLOCKS, GallifreyBlocks.GALLIFREYAN_GRASS_BLOCK, GallifreyBlocks.GALLIFREYAN_SAND);

        tagAppend(GCTags.Blocks.GALLIFREYAN_STONE_REPLACEABLES, GallifreyBlocks.GALLIFREYAN_STONE);

        tagAppend(GCTags.Blocks.BASE_STONE_GALLIFREY, GallifreyBlocks.GALLIFREYAN_STONE);

        tagAddBlockItem(
                BlockItemTags.LOGS_THAT_BURN,
                CadonwoodBlocks.CADONWOOD_LOG,
                CadonwoodBlocks.CADONWOOD_WOOD,
                CadonwoodBlocks.STRIPPED_CADONWOOD_LOG,
                CadonwoodBlocks.STRIPPED_CADONWOOD_WOOD);

        tagAddBlockItem(BlockItemTags.SAPLINGS, CadonwoodBlocks.CADONWOOD_SAPLING);

        tagAdd(BlockTags.DIRT, GallifreyBlocks.GALLIFREYAN_GRASS_BLOCK, GallifreyBlocks.GALLIFREYAN_DIRT);

        tagAddBlockItem(BlockItemTags.LOGS, CadonwoodBlocks.CADONWOOD_LOG, CadonwoodBlocks.STRIPPED_CADONWOOD_LOG);

        tagAdd(
                BlockTags.STAIRS,
                CadonwoodBlocks.CADONWOOD_STAIRS,
                InteriorBlocks.BRACHACKI_SLAB,
                InteriorBlocks.BRACHACKI_STAIRS_2,
                InteriorBlocks.BRACHACKI_STAIRS_3,
                InteriorBlocks.BRACHACKI_STAIRS_4,
                InteriorBlocks.BRACHACKI_TUAT_STAIRS);

        tagAdd(
                BlockTags.SLABS,
                CadonwoodBlocks.CADONWOOD_SLAB,
                InteriorBlocks.BRACHACKI_SLAB,
                InteriorBlocks.BRACHACKI_SLAB_2,
                InteriorBlocks.BRACHACKI_SLAB_3,
                InteriorBlocks.BRACHACKI_SLAB_4,
                InteriorBlocks.BRACHACKI_TUAT_SLAB);

        tagAdd(
                BlockTags.PRESSURE_PLATES,
                CadonwoodBlocks.CADONWOOD_PRESSURE_PLATE,
                InteriorBlocks.BRACHACKI_PRESSURE_PLATE,
                InteriorBlocks.BRACHACKI_PRESSURE_PLATE_2,
                InteriorBlocks.BRACHACKI_PRESSURE_PLATE_3,
                InteriorBlocks.BRACHACKI_PRESSURE_PLATE_4,
                InteriorBlocks.BRACHACKI_TUAT_PRESSURE_PLATE);

        tagAdd(BlockTags.BUTTONS, CadonwoodBlocks.CADONWOOD_BUTTON);

        tagAdd(
                BlockTags.FENCES,
                CadonwoodBlocks.CADONWOOD_FENCE,
                InteriorBlocks.BRACHACKI_FENCE,
                InteriorBlocks.BRACHACKI_FENCE_2,
                InteriorBlocks.BRACHACKI_FENCE_3,
                InteriorBlocks.BRACHACKI_FENCE_4,
                InteriorBlocks.BRACHACKI_TUAT_FENCE);

        tagAdd(
                BlockTags.FENCE_GATES,
                CadonwoodBlocks.CADONWOOD_FENCE_GATE,
                InteriorBlocks.BRACHACKI_FENCE_GATE,
                InteriorBlocks.BRACHACKI_FENCE_GATE_2,
                InteriorBlocks.BRACHACKI_FENCE_GATE_3,
                InteriorBlocks.BRACHACKI_FENCE_GATE_4,
                InteriorBlocks.BRACHACKI_TUAT_FENCE_GATE);

        tagAdd(
                BlockTags.WALLS,
                CadonwoodBlocks.CADONWOOD_WALL,
                InteriorBlocks.BRACHACKI_WALL,
                InteriorBlocks.BRACHACKI_WALL_2,
                InteriorBlocks.BRACHACKI_WALL_3,
                InteriorBlocks.BRACHACKI_WALL_4,
                InteriorBlocks.BRACHACKI_TUAT_WALL);
    }
}

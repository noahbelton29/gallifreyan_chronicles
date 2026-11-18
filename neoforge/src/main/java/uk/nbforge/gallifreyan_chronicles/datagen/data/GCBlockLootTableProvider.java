package uk.nbforge.gallifreyan_chronicles.datagen.data;

import java.util.Set;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.NonNull;
import uk.nbforge.gallifreyan_chronicles.core.GCBlocks;
import uk.nbforge.gallifreyan_chronicles.core.GCItems;
import uk.nbforge.gallifreyan_chronicles.core.block.registry.CadonwoodBlocks;
import uk.nbforge.gallifreyan_chronicles.core.block.registry.FloraBlocks;
import uk.nbforge.gallifreyan_chronicles.core.block.registry.GallifreyBlocks;
import uk.nbforge.gallifreyan_chronicles.core.block.registry.InteriorBlocks;
import uk.nbforge.gallifreyan_chronicles.core.block.registry.MoonBlocks;
import uk.nbforge.gallifreyan_chronicles.core.block.registry.RoundelBlocks;
import uk.nbforge.gallifreyan_chronicles.core.block.registry.TardisBlocks;

public class GCBlockLootTableProvider extends BlockLootSubProvider {
    public GCBlockLootTableProvider(LootTableSubProvider.Context context) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), context);
    }

    @Override
    protected void generate() {
        dropSelf(CadonwoodBlocks.CADONWOOD_PLANKS);
        dropSelf(GallifreyBlocks.GALLIFREYAN_DIRT);
        dropSelf(CadonwoodBlocks.CADONWOOD_WOOD);
        dropSelf(CadonwoodBlocks.STRIPPED_CADONWOOD_LOG);
        dropSelf(CadonwoodBlocks.STRIPPED_CADONWOOD_WOOD);
        dropSelf(GallifreyBlocks.GALLIFREYAN_GRAVEL);
        dropSelf(GallifreyBlocks.ARCADIAN_SHALE);
        dropSelf(InteriorBlocks.BRACHACKI_WALL_BLOCK);
        dropSelf(InteriorBlocks.BRACHACKI_WALL_BLOCK_2);
        dropSelf(InteriorBlocks.BRACHACKI_ROUNDEL_BLOCK);
        RoundelBlocks.WOOD_ROUNDELS.values().forEach(this::dropSelf);
        RoundelBlocks.WOOD_HALF_ROUNDELS.values().forEach(this::dropSelf);
        RoundelBlocks.CONCRETE_ROUNDELS.values().forEach(this::dropSelf);
        RoundelBlocks.CONCRETE_HALF_ROUNDELS.values().forEach(this::dropSelf);
        dropSelf(InteriorBlocks.BRACHACKI_HALF_ROUNDEL_BLOCK);
        dropSelf(InteriorBlocks.BRACHACKI_ROUNDEL_ALT_BLOCK);
        dropSelf(InteriorBlocks.BRACHACKI_HALF_ROUNDEL_ALT_BLOCK);
        dropSelf(InteriorBlocks.BRACHACKI_ROUNDEL_BLOCK_2);
        dropSelf(InteriorBlocks.BRACHACKI_HALF_ROUNDEL_BLOCK_2);
        dropSelf(InteriorBlocks.BRACHACKI_WALL_BLOCK_3);
        dropSelf(InteriorBlocks.BRACHACKI_WALL_BLOCK_4);
        dropSelf(InteriorBlocks.BRACHACKI_ROUNDEL_BLOCK_3);
        dropSelf(InteriorBlocks.BRACHACKI_ROUNDEL_BLOCK_4);
        dropSelf(InteriorBlocks.BRACHACKI_HALF_ROUNDEL_BLOCK_3);
        dropSelf(InteriorBlocks.BRACHACKI_HALF_ROUNDEL_BLOCK_4);
        dropSelf(InteriorBlocks.BRACHACKI_LIGHT_STRIP_BLOCK);
        dropSelf(InteriorBlocks.BRACHACKI_TUAT_WALL_BLOCK);
        dropSelf(InteriorBlocks.BRACHACKI_TUAT_ROUNDEL_BLOCK);
        dropSelf(InteriorBlocks.BRACHACKI_TUAT_HALF_ROUNDEL_BLOCK);
        dropSelf(InteriorBlocks.BRACHACKI_TUAT_HEXAGONAL_ROUNDEL_BLOCK);
        dropSelf(InteriorBlocks.BRACHACKI_TUAT_HALF_HEXAGONAL_ROUNDEL_BLOCK);
        dropSelf(InteriorBlocks.BRACHACKI_FLOOR_BLOCK);
        dropSelf(InteriorBlocks.BRACHACKI_STAIRS);
        add(InteriorBlocks.BRACHACKI_SLAB, this::createSlabItemTable);
        dropSelf(InteriorBlocks.BRACHACKI_PRESSURE_PLATE);
        dropSelf(InteriorBlocks.BRACHACKI_WALL);
        dropSelf(InteriorBlocks.BRACHACKI_FENCE);
        dropSelf(InteriorBlocks.BRACHACKI_FENCE_GATE);
        dropSelf(InteriorBlocks.BRACHACKI_STAIRS_2);
        add(InteriorBlocks.BRACHACKI_SLAB_2, this::createSlabItemTable);
        dropSelf(InteriorBlocks.BRACHACKI_PRESSURE_PLATE_2);
        dropSelf(InteriorBlocks.BRACHACKI_WALL_2);
        dropSelf(InteriorBlocks.BRACHACKI_FENCE_2);
        dropSelf(InteriorBlocks.BRACHACKI_FENCE_GATE_2);
        dropSelf(InteriorBlocks.BRACHACKI_TUAT_FLOOR_BLOCK);
        dropSelf(InteriorBlocks.VOID_BLOCK);
        dropSelf(InteriorBlocks.BRACHACKI_STAIRS_3);
        dropSelf(InteriorBlocks.BRACHACKI_STAIRS_4);
        add(InteriorBlocks.BRACHACKI_SLAB_3, this::createSlabItemTable);
        add(InteriorBlocks.BRACHACKI_SLAB_4, this::createSlabItemTable);
        dropSelf(InteriorBlocks.BRACHACKI_PRESSURE_PLATE_3);
        dropSelf(InteriorBlocks.BRACHACKI_PRESSURE_PLATE_4);
        dropSelf(InteriorBlocks.BRACHACKI_WALL_3);
        dropSelf(InteriorBlocks.BRACHACKI_WALL_4);
        dropSelf(InteriorBlocks.BRACHACKI_FENCE_3);
        dropSelf(InteriorBlocks.BRACHACKI_FENCE_4);
        dropSelf(InteriorBlocks.BRACHACKI_FENCE_GATE_3);
        dropSelf(InteriorBlocks.BRACHACKI_FENCE_GATE_4);
        dropSelf(InteriorBlocks.BRACHACKI_TUAT_STAIRS);
        add(InteriorBlocks.BRACHACKI_TUAT_SLAB, this::createSlabItemTable);
        dropSelf(InteriorBlocks.BRACHACKI_TUAT_PRESSURE_PLATE);
        dropSelf(InteriorBlocks.BRACHACKI_TUAT_WALL);
        dropSelf(InteriorBlocks.BRACHACKI_TUAT_FENCE);
        dropSelf(InteriorBlocks.BRACHACKI_TUAT_FENCE_GATE);
        dropSelf(GallifreyBlocks.GALLIFREYAN_SAND);
        dropSelf(GallifreyBlocks.GALLIFREYAN_MUD);
        dropSelf(CadonwoodBlocks.CADONWOOD_LOG);
        dropSelf(CadonwoodBlocks.CADONWOOD_SAPLING);
        dropSelf(GallifreyBlocks.GALLIFREYAN_COBBLESTONE);
        dropSelf(FloraBlocks.GALLIFREYAN_VINE);
        dropSelf(GallifreyBlocks.COBBLED_LIMESTONE);
        dropSelf(FloraBlocks.THORIVINE);
        dropSelf(FloraBlocks.THORIVINE_FLOWER);
        dropSelf(GallifreyBlocks.GALLIFREYAN_MUDSTONE);
        dropSelf(MoonBlocks.LUNAR_REGOLITH);
        dropSelf(MoonBlocks.MOONSLATE);
        dropSelf(MoonBlocks.MOON_DUST);
        dropSelf(TardisBlocks.TARDIS);
        dropSelf(TardisBlocks.TARDIS_CONSOLE);

        add(CadonwoodBlocks.POTTED_CADONWOOD_SAPLING, createPotFlowerItemTable(CadonwoodBlocks.CADONWOOD_SAPLING));
        add(GallifreyBlocks.GALLIFREYAN_STONE, createSingleItemTable(GallifreyBlocks.GALLIFREYAN_COBBLESTONE));
        add(GallifreyBlocks.GALLIFREYAN_GRASS_BLOCK, createSingleItemTable(GallifreyBlocks.GALLIFREYAN_DIRT));
        add(
                CadonwoodBlocks.CADONWOOD_LEAVES,
                createLeavesDrops(
                        CadonwoodBlocks.CADONWOOD_LEAVES,
                        CadonwoodBlocks.CADONWOOD_SAPLING,
                        NORMAL_LEAVES_SAPLING_CHANCES));

        add(FloraBlocks.SHORT_GALLIFREYAN_DRY_GRASS, createGrassDrops(FloraBlocks.SHORT_GALLIFREYAN_DRY_GRASS));
        add(FloraBlocks.SHORT_GALLIFREYAN_GRASS, createGrassDrops(FloraBlocks.SHORT_GALLIFREYAN_GRASS));
        add(FloraBlocks.TALL_GALLIFREYAN_GRASS, createGrassDrops(FloraBlocks.TALL_GALLIFREYAN_GRASS));
        add(FloraBlocks.TALL_GALLIFREYAN_DRY_GRASS, createGrassDrops(FloraBlocks.TALL_GALLIFREYAN_DRY_GRASS));

        add(
                GallifreyBlocks.GALLIFREYAN_STONE_IRON_ORE,
                createOreDrop(GallifreyBlocks.GALLIFREYAN_STONE_IRON_ORE, Items.RAW_IRON));
        add(
                GallifreyBlocks.GALLIFREYAN_STONE_COAL_ORE,
                createOreDrop(GallifreyBlocks.GALLIFREYAN_STONE_COAL_ORE, Items.COAL));
        add(
                GallifreyBlocks.ARCADIAN_SHALE_TARANIUM_ORE,
                createOreDrop(GallifreyBlocks.ARCADIAN_SHALE_TARANIUM_ORE, GCItems.TARANIUM_SCRAP));

        dropSelf(CadonwoodBlocks.CADONWOOD_STAIRS);
        add(CadonwoodBlocks.CADONWOOD_SLAB, this::createSlabItemTable);

        dropSelf(CadonwoodBlocks.CADONWOOD_PRESSURE_PLATE);
        dropSelf(CadonwoodBlocks.CADONWOOD_BUTTON);

        dropSelf(CadonwoodBlocks.CADONWOOD_FENCE);
        dropSelf(CadonwoodBlocks.CADONWOOD_FENCE_GATE);
        dropSelf(CadonwoodBlocks.CADONWOOD_WALL);
    }

    @Override
    protected @NonNull Iterable<Block> getKnownBlocks() {
        return GCBlocks.BLOCKS;
    }
}

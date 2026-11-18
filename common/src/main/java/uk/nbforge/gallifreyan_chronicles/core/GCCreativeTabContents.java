package uk.nbforge.gallifreyan_chronicles.core;

import java.util.function.Consumer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.core.block.registry.CadonwoodBlocks;
import uk.nbforge.gallifreyan_chronicles.core.block.registry.FloraBlocks;
import uk.nbforge.gallifreyan_chronicles.core.block.registry.GallifreyBlocks;
import uk.nbforge.gallifreyan_chronicles.core.block.registry.InteriorBlocks;
import uk.nbforge.gallifreyan_chronicles.core.block.registry.RoundelBlocks;
import uk.nbforge.gallifreyan_chronicles.core.block.registry.TardisBlocks;

public final class GCCreativeTabContents {
    private GCCreativeTabContents() {}

    public static void tardis(Consumer<ItemStack> output) {
        add(output, TardisBlocks.TARDIS);
        ItemStack brachackiConsole = new ItemStack(TardisBlocks.TARDIS_CONSOLE);
        brachackiConsole.set(GCDataComponents.CONSOLE_ID, Constants.MOD_ID + ":brachacki_console");
        output.accept(brachackiConsole);
        add(output, GCItems.TARDIS_KEY);
        add(output, GCItems.MASTER_TARDIS_KEY);
        add(output, GCItems.MALLET);
        add(output, GCItems.JELLY_BABIES);
        add(output, GCItems.STATTENHEIM_REMOTE);
        add(output, TardisBlocks.TARDIS_INTERIOR_DOOR);
        GCBlocks.pickableDoors().stream().findFirst().ifPresent(door -> add(output, door));
        add(output, InteriorBlocks.BRACHACKI_FLOOR_BLOCK);
        add(output, InteriorBlocks.BRACHACKI_WALL_BLOCK);
        add(output, InteriorBlocks.BRACHACKI_ROUNDEL_BLOCK);
        add(output, InteriorBlocks.BRACHACKI_HALF_ROUNDEL_BLOCK);
        add(output, InteriorBlocks.BRACHACKI_ROUNDEL_ALT_BLOCK);
        add(output, InteriorBlocks.BRACHACKI_HALF_ROUNDEL_ALT_BLOCK);
        add(output, InteriorBlocks.BRACHACKI_WALL_BLOCK_2);
        add(output, InteriorBlocks.BRACHACKI_ROUNDEL_BLOCK_2);
        add(output, InteriorBlocks.BRACHACKI_HALF_ROUNDEL_BLOCK_2);
        add(output, InteriorBlocks.BRACHACKI_WALL_BLOCK_3);
        add(output, InteriorBlocks.BRACHACKI_WALL_BLOCK_4);
        add(output, InteriorBlocks.BRACHACKI_ROUNDEL_BLOCK_3);
        add(output, InteriorBlocks.BRACHACKI_ROUNDEL_BLOCK_4);
        add(output, InteriorBlocks.BRACHACKI_HALF_ROUNDEL_BLOCK_3);
        add(output, InteriorBlocks.BRACHACKI_HALF_ROUNDEL_BLOCK_4);
        add(output, InteriorBlocks.BRACHACKI_LIGHT_STRIP_BLOCK);
        add(output, InteriorBlocks.VOID_BLOCK);
        add(output, InteriorBlocks.BRACHACKI_TUAT_FLOOR_BLOCK);
        add(output, InteriorBlocks.BRACHACKI_TUAT_WALL_BLOCK);
        add(output, InteriorBlocks.BRACHACKI_TUAT_ROUNDEL_BLOCK);
        add(output, InteriorBlocks.BRACHACKI_TUAT_HALF_ROUNDEL_BLOCK);
        add(output, InteriorBlocks.BRACHACKI_TUAT_HEXAGONAL_ROUNDEL_BLOCK);
        add(output, InteriorBlocks.BRACHACKI_TUAT_HALF_HEXAGONAL_ROUNDEL_BLOCK);
        add(output, InteriorBlocks.BRACHACKI_STAIRS);
        add(output, InteriorBlocks.BRACHACKI_SLAB);
        add(output, InteriorBlocks.BRACHACKI_PRESSURE_PLATE);
        add(output, InteriorBlocks.BRACHACKI_WALL);
        add(output, InteriorBlocks.BRACHACKI_FENCE);
        add(output, InteriorBlocks.BRACHACKI_FENCE_GATE);
        add(output, InteriorBlocks.BRACHACKI_STAIRS_2);
        add(output, InteriorBlocks.BRACHACKI_SLAB_2);
        add(output, InteriorBlocks.BRACHACKI_PRESSURE_PLATE_2);
        add(output, InteriorBlocks.BRACHACKI_WALL_2);
        add(output, InteriorBlocks.BRACHACKI_FENCE_2);
        add(output, InteriorBlocks.BRACHACKI_FENCE_GATE_2);
        add(output, InteriorBlocks.BRACHACKI_STAIRS_3);
        add(output, InteriorBlocks.BRACHACKI_STAIRS_4);
        add(output, InteriorBlocks.BRACHACKI_SLAB_3);
        add(output, InteriorBlocks.BRACHACKI_SLAB_4);
        add(output, InteriorBlocks.BRACHACKI_PRESSURE_PLATE_3);
        add(output, InteriorBlocks.BRACHACKI_PRESSURE_PLATE_4);
        add(output, InteriorBlocks.BRACHACKI_WALL_3);
        add(output, InteriorBlocks.BRACHACKI_WALL_4);
        add(output, InteriorBlocks.BRACHACKI_FENCE_3);
        add(output, InteriorBlocks.BRACHACKI_FENCE_4);
        add(output, InteriorBlocks.BRACHACKI_FENCE_GATE_3);
        add(output, InteriorBlocks.BRACHACKI_FENCE_GATE_4);
        add(output, InteriorBlocks.BRACHACKI_TUAT_STAIRS);
        add(output, InteriorBlocks.BRACHACKI_TUAT_SLAB);
        add(output, InteriorBlocks.BRACHACKI_TUAT_PRESSURE_PLATE);
        add(output, InteriorBlocks.BRACHACKI_TUAT_WALL);
        add(output, InteriorBlocks.BRACHACKI_TUAT_FENCE);
        add(output, InteriorBlocks.BRACHACKI_TUAT_FENCE_GATE);
    }

    public static void naturalBlocks(Consumer<ItemStack> output) {
        add(output, GallifreyBlocks.GALLIFREYAN_GRASS_BLOCK);
        add(output, GallifreyBlocks.GALLIFREYAN_DIRT);
        add(output, GallifreyBlocks.GALLIFREYAN_MUD);
        add(output, GallifreyBlocks.GALLIFREYAN_MUDSTONE);
        add(output, GallifreyBlocks.GALLIFREYAN_SAND);
        add(output, GallifreyBlocks.GALLIFREYAN_GRAVEL);

        add(output, GallifreyBlocks.GALLIFREYAN_STONE);
        add(output, GallifreyBlocks.GALLIFREYAN_COBBLESTONE);
        add(output, GallifreyBlocks.COBBLED_LIMESTONE);
        add(output, GallifreyBlocks.ARCADIAN_SHALE);

        add(output, GallifreyBlocks.GALLIFREYAN_STONE_COAL_ORE);
        add(output, GallifreyBlocks.GALLIFREYAN_STONE_IRON_ORE);
        add(output, GallifreyBlocks.ARCADIAN_SHALE_TARANIUM_ORE);
    }

    public static void buildingBlocks(Consumer<ItemStack> output) {
        add(output, CadonwoodBlocks.CADONWOOD_LOG);
        add(output, CadonwoodBlocks.CADONWOOD_WOOD);
        add(output, CadonwoodBlocks.STRIPPED_CADONWOOD_LOG);
        add(output, CadonwoodBlocks.STRIPPED_CADONWOOD_WOOD);
        add(output, CadonwoodBlocks.CADONWOOD_PLANKS);
        add(output, CadonwoodBlocks.CADONWOOD_STAIRS);
        add(output, CadonwoodBlocks.CADONWOOD_SLAB);
        add(output, CadonwoodBlocks.CADONWOOD_FENCE);
        add(output, CadonwoodBlocks.CADONWOOD_FENCE_GATE);
        add(output, CadonwoodBlocks.CADONWOOD_WALL);
        add(output, CadonwoodBlocks.CADONWOOD_PRESSURE_PLATE);
        add(output, CadonwoodBlocks.CADONWOOD_BUTTON);
        for (String wood : RoundelBlocks.ROUNDEL_WOODS) {
            add(output, RoundelBlocks.WOOD_ROUNDELS.get(wood));
            add(output, RoundelBlocks.WOOD_HALF_ROUNDELS.get(wood));
        }
        for (String colour : RoundelBlocks.ROUNDEL_CONCRETES) {
            add(output, RoundelBlocks.CONCRETE_ROUNDELS.get(colour));
            add(output, RoundelBlocks.CONCRETE_HALF_ROUNDELS.get(colour));
        }
    }

    public static void flora(Consumer<ItemStack> output) {
        add(output, CadonwoodBlocks.CADONWOOD_SAPLING);
        add(output, CadonwoodBlocks.CADONWOOD_LEAVES);
        add(output, FloraBlocks.GALLIFREYAN_VINE);
        add(output, FloraBlocks.SHORT_GALLIFREYAN_GRASS);
        add(output, FloraBlocks.TALL_GALLIFREYAN_GRASS);
        add(output, FloraBlocks.SHORT_GALLIFREYAN_DRY_GRASS);
        add(output, FloraBlocks.TALL_GALLIFREYAN_DRY_GRASS);
        add(output, FloraBlocks.THORIVINE);
        add(output, FloraBlocks.THORIVINE_FLOWER);
    }

    public static void toolsAndArmor(Consumer<ItemStack> output) {
        add(output, GCItems.RAW_TIN);

        add(output, GCItems.TARANIUM_SCRAP);
        add(output, GCItems.TARANIUM_CRYSTAL);
        add(output, GCItems.REFINED_TARANIUM_CRYSTAL);
        add(output, GCItems.TARANIUM_INGOT);

        add(output, GCItems.TARANIUM_SWORD);
        add(output, GCItems.TARANIUM_PICKAXE);
        add(output, GCItems.TARANIUM_AXE);
        add(output, GCItems.TARANIUM_SHOVEL);
        add(output, GCItems.TARANIUM_HOE);

        add(output, GCItems.TARANIUM_HELMET);
        add(output, GCItems.TARANIUM_CHESTPLATE);
        add(output, GCItems.TARANIUM_LEGGINGS);
        add(output, GCItems.TARANIUM_BOOTS);
    }

    private static void add(Consumer<ItemStack> output, ItemLike item) {
        output.accept(new ItemStack(item));
    }
}

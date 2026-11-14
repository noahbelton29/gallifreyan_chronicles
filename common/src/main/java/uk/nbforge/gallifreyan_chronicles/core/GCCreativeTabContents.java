package uk.nbforge.gallifreyan_chronicles.core;

import uk.nbforge.gallifreyan_chronicles.Constants;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.function.Consumer;

public final class GCCreativeTabContents {
    private GCCreativeTabContents() {
    }

    public static void tardis(Consumer<ItemStack> output) {
        add(output, GCBlocks.TARDIS);
        ItemStack brachackiConsole = new ItemStack(GCBlocks.TARDIS_CONSOLE);
        brachackiConsole.set(GCDataComponents.CONSOLE_ID, Constants.MOD_ID + ":brachacki_console");
        output.accept(brachackiConsole);
        add(output, GCItems.TARDIS_KEY);
        add(output, GCItems.MASTER_TARDIS_KEY);
        add(output, GCItems.MALLET);
        add(output, GCItems.JELLY_BABIES);
        add(output, GCItems.STATTENHEIM_REMOTE);
        add(output, GCBlocks.TARDIS_INTERIOR_DOOR);
        GCBlocks.pickableDoors().stream().findFirst().ifPresent(door -> add(output, door));
        add(output, GCBlocks.BRACHACKI_FLOOR_BLOCK);
        add(output, GCBlocks.BRACHACKI_WALL_BLOCK);
        add(output, GCBlocks.BRACHACKI_ROUNDEL_BLOCK);
        add(output, GCBlocks.BRACHACKI_HALF_ROUNDEL_BLOCK);
        add(output, GCBlocks.BRACHACKI_ROUNDEL_ALT_BLOCK);
        add(output, GCBlocks.BRACHACKI_HALF_ROUNDEL_ALT_BLOCK);
        add(output, GCBlocks.BRACHACKI_WALL_BLOCK_2);
        add(output, GCBlocks.BRACHACKI_ROUNDEL_BLOCK_2);
        add(output, GCBlocks.BRACHACKI_HALF_ROUNDEL_BLOCK_2);
        add(output, GCBlocks.BRACHACKI_WALL_BLOCK_3);
        add(output, GCBlocks.BRACHACKI_WALL_BLOCK_4);
        add(output, GCBlocks.BRACHACKI_ROUNDEL_BLOCK_3);
        add(output, GCBlocks.BRACHACKI_ROUNDEL_BLOCK_4);
        add(output, GCBlocks.BRACHACKI_HALF_ROUNDEL_BLOCK_3);
        add(output, GCBlocks.BRACHACKI_HALF_ROUNDEL_BLOCK_4);
        add(output, GCBlocks.BRACHACKI_LIGHT_STRIP_BLOCK);
        add(output, GCBlocks.VOID_BLOCK);
        add(output, GCBlocks.BRACHACKI_TUAT_FLOOR_BLOCK);
        add(output, GCBlocks.BRACHACKI_TUAT_WALL_BLOCK);
        add(output, GCBlocks.BRACHACKI_TUAT_ROUNDEL_BLOCK);
        add(output, GCBlocks.BRACHACKI_TUAT_HALF_ROUNDEL_BLOCK);
        add(output, GCBlocks.BRACHACKI_TUAT_HEXAGONAL_ROUNDEL_BLOCK);
        add(output, GCBlocks.BRACHACKI_TUAT_HALF_HEXAGONAL_ROUNDEL_BLOCK);
        add(output, GCBlocks.BRACHACKI_STAIRS);
        add(output, GCBlocks.BRACHACKI_SLAB);
        add(output, GCBlocks.BRACHACKI_PRESSURE_PLATE);
        add(output, GCBlocks.BRACHACKI_WALL);
        add(output, GCBlocks.BRACHACKI_FENCE);
        add(output, GCBlocks.BRACHACKI_FENCE_GATE);
        add(output, GCBlocks.BRACHACKI_STAIRS_2);
        add(output, GCBlocks.BRACHACKI_SLAB_2);
        add(output, GCBlocks.BRACHACKI_PRESSURE_PLATE_2);
        add(output, GCBlocks.BRACHACKI_WALL_2);
        add(output, GCBlocks.BRACHACKI_FENCE_2);
        add(output, GCBlocks.BRACHACKI_FENCE_GATE_2);
        add(output, GCBlocks.BRACHACKI_STAIRS_3);
        add(output, GCBlocks.BRACHACKI_STAIRS_4);
        add(output, GCBlocks.BRACHACKI_SLAB_3);
        add(output, GCBlocks.BRACHACKI_SLAB_4);
        add(output, GCBlocks.BRACHACKI_PRESSURE_PLATE_3);
        add(output, GCBlocks.BRACHACKI_PRESSURE_PLATE_4);
        add(output, GCBlocks.BRACHACKI_WALL_3);
        add(output, GCBlocks.BRACHACKI_WALL_4);
        add(output, GCBlocks.BRACHACKI_FENCE_3);
        add(output, GCBlocks.BRACHACKI_FENCE_4);
        add(output, GCBlocks.BRACHACKI_FENCE_GATE_3);
        add(output, GCBlocks.BRACHACKI_FENCE_GATE_4);
        add(output, GCBlocks.BRACHACKI_TUAT_STAIRS);
        add(output, GCBlocks.BRACHACKI_TUAT_SLAB);
        add(output, GCBlocks.BRACHACKI_TUAT_PRESSURE_PLATE);
        add(output, GCBlocks.BRACHACKI_TUAT_WALL);
        add(output, GCBlocks.BRACHACKI_TUAT_FENCE);
        add(output, GCBlocks.BRACHACKI_TUAT_FENCE_GATE);

    }

    public static void naturalBlocks(Consumer<ItemStack> output) {
        add(output, GCBlocks.GALLIFREYAN_GRASS_BLOCK);
        add(output, GCBlocks.GALLIFREYAN_DIRT);
        add(output, GCBlocks.GALLIFREYAN_MUD);
        add(output, GCBlocks.GALLIFREYAN_MUDSTONE);
        add(output, GCBlocks.GALLIFREYAN_SAND);
        add(output, GCBlocks.GALLIFREYAN_GRAVEL);

        add(output, GCBlocks.GALLIFREYAN_STONE);
        add(output, GCBlocks.GALLIFREYAN_COBBLESTONE);
        add(output, GCBlocks.COBBLED_LIMESTONE);
        add(output, GCBlocks.ARCADIAN_SHALE);

        add(output, GCBlocks.GALLIFREYAN_STONE_COAL_ORE);
        add(output, GCBlocks.GALLIFREYAN_STONE_IRON_ORE);
        add(output, GCBlocks.ARCADIAN_SHALE_TARANIUM_ORE);

    }

    public static void buildingBlocks(Consumer<ItemStack> output) {
        add(output, GCBlocks.CADONWOOD_LOG);
        add(output, GCBlocks.CADONWOOD_WOOD);
        add(output, GCBlocks.STRIPPED_CADONWOOD_LOG);
        add(output, GCBlocks.STRIPPED_CADONWOOD_WOOD);
        add(output, GCBlocks.CADONWOOD_PLANKS);
        add(output, GCBlocks.CADONWOOD_STAIRS);
        add(output, GCBlocks.CADONWOOD_SLAB);
        add(output, GCBlocks.CADONWOOD_FENCE);
        add(output, GCBlocks.CADONWOOD_FENCE_GATE);
        add(output, GCBlocks.CADONWOOD_WALL);
        add(output, GCBlocks.CADONWOOD_PRESSURE_PLATE);
        add(output, GCBlocks.CADONWOOD_BUTTON);
        for (String wood : GCBlocks.ROUNDEL_WOODS) {
            add(output, GCBlocks.WOOD_ROUNDELS.get(wood));
            add(output, GCBlocks.WOOD_HALF_ROUNDELS.get(wood));
        }
        for (String colour : GCBlocks.ROUNDEL_CONCRETES) {
            add(output, GCBlocks.CONCRETE_ROUNDELS.get(colour));
            add(output, GCBlocks.CONCRETE_HALF_ROUNDELS.get(colour));
        }

    }

    public static void flora(Consumer<ItemStack> output) {
        add(output, GCBlocks.CADONWOOD_SAPLING);
        add(output, GCBlocks.CADONWOOD_LEAVES);
        add(output, GCBlocks.GALLIFREYAN_VINE);
        add(output, GCBlocks.SHORT_GALLIFREYAN_GRASS);
        add(output, GCBlocks.TALL_GALLIFREYAN_GRASS);
        add(output, GCBlocks.SHORT_GALLIFREYAN_DRY_GRASS);
        add(output, GCBlocks.TALL_GALLIFREYAN_DRY_GRASS);
        add(output, GCBlocks.THORIVINE);
        add(output, GCBlocks.THORIVINE_FLOWER);

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

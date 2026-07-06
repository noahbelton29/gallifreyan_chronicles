package com.noahtnt2009.gallifreyan_chronicles.init;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.util.GCUtils;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class GCCreativeModeTabs {
    public static final CreativeModeTab TARDIS_TAB = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
            GCUtils.of("tardis"),
            FabricCreativeModeTab.builder().icon(() -> new ItemStack(GCBlocks.TARDIS))
                    .title(Component.translatable("creativetab.gallifreyan_chronicles.tardis"))
                    .displayItems((parameters, output) -> {
                        output.accept(GCBlocks.TARDIS);
                    }).build());

    public static final CreativeModeTab NATURAL_BLOCKS_TAB = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
            GCUtils.of("natural_blocks"),
            FabricCreativeModeTab.builder().icon(() -> new ItemStack(GCBlocks.GALLIFREYAN_GRASS_BLOCK))
                    .title(Component.translatable("creativetab.gallifreyan_chronicles.natural_blocks"))
                    .displayItems((parameters, output) -> {
                        output.accept(GCBlocks.GALLIFREYAN_GRASS_BLOCK);
                        output.accept(GCBlocks.GALLIFREYAN_DIRT);
                        output.accept(GCBlocks.GALLIFREYAN_MUD);
                        output.accept(GCBlocks.GALLIFREYAN_MUDSTONE);
                        output.accept(GCBlocks.GALLIFREYAN_SAND);
                        output.accept(GCBlocks.GALLIFREYAN_GRAVEL);

                        output.accept(GCBlocks.GALLIFREYAN_STONE);
                        output.accept(GCBlocks.GALLIFREYAN_COBBLESTONE);
                        output.accept(GCBlocks.COBBLED_LIMESTONE);
                        output.accept(GCBlocks.ARCADIAN_SHALE);

                        output.accept(GCBlocks.GALLIFREYAN_STONE_COAL_ORE);
                        output.accept(GCBlocks.GALLIFREYAN_STONE_IRON_ORE);
                        output.accept(GCBlocks.ARCADIAN_SHALE_TARANIUM_ORE);
                    }).build());

    public static final CreativeModeTab BUILDING_BLOCKS_TAB = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
            GCUtils.of("building_bloccks"),
            FabricCreativeModeTab.builder().icon(() -> new ItemStack(GCBlocks.CADONWOOD_PLANKS))
                    .title(Component.translatable("creativetab.gallifreyan_chronicles.building_blocks"))
                    .displayItems((parameters, output) -> {
                        output.accept(GCBlocks.CADONWOOD_LOG);
                        output.accept(GCBlocks.CADONWOOD_WOOD);
                        output.accept(GCBlocks.STRIPPED_CADONWOOD_LOG);
                        output.accept(GCBlocks.STRIPPED_CADONWOOD_WOOD);
                        output.accept(GCBlocks.CADONWOOD_PLANKS);
                        output.accept(GCBlocks.CADONWOOD_STAIRS);
                        output.accept(GCBlocks.CADONWOOD_SLAB);
                        output.accept(GCBlocks.CADONWOOD_FENCE);
                        output.accept(GCBlocks.CADONWOOD_FENCE_GATE);
                        output.accept(GCBlocks.CADONWOOD_WALL);
                        output.accept(GCBlocks.CADONWOOD_PRESSURE_PLATE);
                        output.accept(GCBlocks.CADONWOOD_BUTTON);
                    }).build());

    public static final CreativeModeTab FLORA_TAB = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
            GCUtils.of("flora"),
            FabricCreativeModeTab.builder().icon(() -> new ItemStack(GCBlocks.CADONWOOD_LEAVES))
                    .title(Component.translatable("creativetab.gallifreyan_chronicles.flora"))
                    .displayItems((parameters, output) -> {
                        output.accept(GCBlocks.CADONWOOD_SAPLING);
                        output.accept(GCBlocks.CADONWOOD_LEAVES);
                        output.accept(GCBlocks.GALLIFREYAN_VINE);
                        output.accept(GCBlocks.SHORT_GALLIFREYAN_GRASS);
                        output.accept(GCBlocks.TALL_GALLIFREYAN_GRASS);
                        output.accept(GCBlocks.SHORT_GALLIFREYAN_DRY_GRASS);
                        output.accept(GCBlocks.TALL_GALLIFREYAN_DRY_GRASS);
                        output.accept(GCBlocks.THORIVINE);
                        output.accept(GCBlocks.THORIVINE_FLOWER);
                    }).build());

    public static final CreativeModeTab TOOLS_AND_ARMOR_TAB = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
            GCUtils.of("tools_and_armor"),
            FabricCreativeModeTab.builder().icon(() -> new ItemStack(GCItems.TARANIUM_PICKAXE))
                    .title(Component.translatable("creativetab.gallifreyan_chronicles.tools_and_armor"))
                    .displayItems((parameters, output) -> {
                        output.accept(GCItems.TARANIUM_SCRAP);
                        output.accept(GCItems.TARANIUM_CRYSTAL);
                        output.accept(GCItems.REFINED_TARANIUM_CRYSTAL);
                        output.accept(GCItems.TARANIUM_INGOT);

                        output.accept(GCItems.TARANIUM_SWORD);
                        output.accept(GCItems.TARANIUM_PICKAXE);
                        output.accept(GCItems.TARANIUM_AXE);
                        output.accept(GCItems.TARANIUM_SHOVEL);
                        output.accept(GCItems.TARANIUM_HOE);

                        output.accept(GCItems.TARANIUM_HELMET);
                        output.accept(GCItems.TARANIUM_CHESTPLATE);
                        output.accept(GCItems.TARANIUM_LEGGINGS);
                        output.accept(GCItems.TARANIUM_BOOTS);
                    }).build());

    public static void registerFabricCreativeModeTabs() {
        Constants.LOG.info("Registering GC Creative Mode Tabs");
    }
}
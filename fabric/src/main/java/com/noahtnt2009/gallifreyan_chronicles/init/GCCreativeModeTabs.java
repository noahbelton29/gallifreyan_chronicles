package com.noahtnt2009.gallifreyan_chronicles.init;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.util.GCUtils;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import com.noahtnt2009.gallifreyan_chronicles.addon.AddonContent;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class GCCreativeModeTabs {
    public static final CreativeModeTab TARDIS_TAB = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
            GCUtils.modIdentifier("tardis"),
            FabricCreativeModeTab.builder().icon(() -> new ItemStack(GCBlocks.TARDIS))
                    .title(Component.translatable("creativetab.gallifreyan_chronicles.tardis"))
                    .displayItems((parameters, output) -> GCCreativeTabContents.tardis(output::accept))
                    .build());

    public static final CreativeModeTab NATURAL_BLOCKS_TAB = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
            GCUtils.modIdentifier("natural_blocks"),
            FabricCreativeModeTab.builder().icon(() -> new ItemStack(GCBlocks.GALLIFREYAN_GRASS_BLOCK))
                    .title(Component.translatable("creativetab.gallifreyan_chronicles.natural_blocks"))
                    .displayItems((parameters, output) -> GCCreativeTabContents.naturalBlocks(output::accept))
                    .build());

    public static final CreativeModeTab BUILDING_BLOCKS_TAB = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
            GCUtils.modIdentifier("building_blocks"),
            FabricCreativeModeTab.builder().icon(() -> new ItemStack(GCBlocks.CADONWOOD_PLANKS))
                    .title(Component.translatable("creativetab.gallifreyan_chronicles.building_blocks"))
                    .displayItems((parameters, output) -> GCCreativeTabContents.buildingBlocks(output::accept))
                    .build());

    public static final CreativeModeTab FLORA_TAB = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
            GCUtils.modIdentifier("flora"),
            FabricCreativeModeTab.builder().icon(() -> new ItemStack(GCBlocks.CADONWOOD_LEAVES))
                    .title(Component.translatable("creativetab.gallifreyan_chronicles.flora"))
                    .displayItems((parameters, output) -> GCCreativeTabContents.flora(output::accept))
                    .build());

    public static final CreativeModeTab TOOLS_AND_ARMOR_TAB = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
            GCUtils.modIdentifier("tools_and_armor"),
            FabricCreativeModeTab.builder().icon(() -> new ItemStack(GCItems.TARANIUM_PICKAXE))
                    .title(Component.translatable("creativetab.gallifreyan_chronicles.tools_and_armor"))
                    .displayItems((parameters, output) -> GCCreativeTabContents.toolsAndArmor(output::accept))
                    .build());

    /** Everything addons registered through the addon API. Read when the tab is built, after addons have started. */
    public static final CreativeModeTab ADDONS_TAB = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
            GCUtils.modIdentifier("addons"),
            FabricCreativeModeTab.builder().icon(() -> new ItemStack(GCItems.TARANIUM_INGOT))
                    .title(Component.translatable("creativetab.gallifreyan_chronicles.addons"))
                    .displayItems((parameters, output) -> AddonContent.tabItems().forEach(output::accept))
                    .build());

    public static void registerFabricCreativeModeTabs() {
        Constants.LOG.info("Registering GC Creative Mode Tabs (Fabric)");
    }
}
package com.noahtnt2009.gallifreyan_chronicles.registry;

import com.noahtnt2009.gallifreyan_chronicles.GallifreyanChronicles;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class GCCreativeModeTabs {
    public static final CreativeModeTab PLANET_ITEMS_TAB = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
            Identifier.fromNamespaceAndPath(GallifreyanChronicles.MOD_ID, "planet_items"),
            FabricCreativeModeTab.builder().icon(() -> new ItemStack(GCBlocks.GALLIFREYAN_SAND))
                    .title(Component.translatable("creativetab.gallifreyan_chronicles.planet_items"))
                    .displayItems((parameters, output) -> {
                        output.accept(GCBlocks.GALLIFREYAN_GRASS_BLOCK);
                        output.accept(GCBlocks.GALLIFREYAN_DIRT);
                        output.accept(GCBlocks.GALLIFREYAN_SAND);
                        output.accept(GCBlocks.GALLIFREYAN_MUD);
                        output.accept(GCBlocks.GALLIFREYAN_GRAVEL);
                        output.accept(GCBlocks.GALLIFREYAN_STONE);
                        output.accept(GCBlocks.GALLIFREYAN_STONE_COAL_ORE);
                        output.accept(GCBlocks.GALLIFREYAN_STONE_IRON_ORE);
                        output.accept(GCBlocks.GALLIFREYAN_COBBLESTONE);
                        output.accept(GCBlocks.ARCADIAN_SHALE);
                        output.accept(GCBlocks.SHORT_GALLIFREYAN_GRASS);
                        output.accept(GCBlocks.TALL_GALLIFREYAN_GRASS);
                        output.accept(GCBlocks.SHORT_GALLIFREYAN_DRY_GRASS);
                        output.accept(GCBlocks.TALL_GALLIFREYAN_DRY_GRASS);
                        output.accept(GCBlocks.CADONWOOD_LOG);
                        output.accept(GCBlocks.STRIPPED_CADONWOOD_LOG);
                        output.accept(GCBlocks.CADONWOOD_PLANKS);
                        output.accept(GCBlocks.CADONWOOD_LEAVES);
                    }).build());


    public static void init() {
        GallifreyanChronicles.LOGGER.info("Registering Creative Mode Tabs for " + GallifreyanChronicles.MOD_ID);
    }
}

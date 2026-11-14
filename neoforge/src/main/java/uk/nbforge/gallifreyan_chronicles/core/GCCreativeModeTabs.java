package uk.nbforge.gallifreyan_chronicles.core;

import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.addon.AddonContent;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class GCCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Constants.MOD_ID);

    public static final Supplier<CreativeModeTab> TARDIS_TAB = CREATIVE_MODE_TABS.register("tardis",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(GCBlocks.TARDIS))
                    .title(Component.translatable("creativetab.gallifreyan_chronicles.tardis"))
                    .displayItems((params, output) -> GCCreativeTabContents.tardis(output::accept))
                    .build());

    public static final Supplier<CreativeModeTab> NATURAL_BLOCKS_TAB = CREATIVE_MODE_TABS.register("natural_blocks",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(GCBlocks.GALLIFREYAN_GRASS_BLOCK))
                    .title(Component.translatable("creativetab.gallifreyan_chronicles.natural_blocks"))
                    .displayItems((params, output) -> GCCreativeTabContents.naturalBlocks(output::accept))
                    .build());

    public static final Supplier<CreativeModeTab> BUILDING_BLOCKS_TAB = CREATIVE_MODE_TABS.register("building_blocks",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(GCBlocks.CADONWOOD_PLANKS))
                    .title(Component.translatable("creativetab.gallifreyan_chronicles.building_blocks"))
                    .displayItems((params, output) -> GCCreativeTabContents.buildingBlocks(output::accept))
                    .build());

    public static final Supplier<CreativeModeTab> FLORA_TAB = CREATIVE_MODE_TABS.register("flora",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(GCBlocks.CADONWOOD_LEAVES))
                    .title(Component.translatable("creativetab.gallifreyan_chronicles.flora"))
                    .displayItems((params, output) -> GCCreativeTabContents.flora(output::accept))
                    .build());

    public static final Supplier<CreativeModeTab> TOOLS_AND_ARMOR_TAB = CREATIVE_MODE_TABS.register("tools_and_armor",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(GCItems.TARANIUM_PICKAXE))
                    .title(Component.translatable("creativetab.gallifreyan_chronicles.tools_and_armor"))
                    .displayItems((params, output) -> GCCreativeTabContents.toolsAndArmor(output::accept))
                    .build());

    /**
     * Everything addons registered through the addon API. Read when the tab is built, after addons have started.
     */
    public static final Supplier<CreativeModeTab> ADDONS_TAB = CREATIVE_MODE_TABS.register("addons",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(GCItems.TARANIUM_INGOT))
                    .title(Component.translatable("creativetab.gallifreyan_chronicles.addons"))
                    .displayItems((params, output) -> AddonContent.tabItems().forEach(output::accept))
                    .build());

    public static void registerNeoForgeCreativeModeTabs(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
        Constants.LOG.info("Registering GC Creative Mode Tabs (NeoForge)");
    }
}
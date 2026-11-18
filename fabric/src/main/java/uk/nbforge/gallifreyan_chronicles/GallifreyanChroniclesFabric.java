package uk.nbforge.gallifreyan_chronicles;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.ItemComponentTooltipProviderRegistry;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import uk.nbforge.gallifreyan_chronicles.core.GCBlockEntities;
import uk.nbforge.gallifreyan_chronicles.core.GCCreativeModeTabs;
import uk.nbforge.gallifreyan_chronicles.core.GCDataComponents;
import uk.nbforge.gallifreyan_chronicles.core.GCEntityTypes;
import uk.nbforge.gallifreyan_chronicles.core.GCEvents;
import uk.nbforge.gallifreyan_chronicles.core.GCFabricCommands;
import uk.nbforge.gallifreyan_chronicles.core.GCFabricWorldGeneration;
import uk.nbforge.gallifreyan_chronicles.core.GCGameRuleCategories;
import uk.nbforge.gallifreyan_chronicles.loader.GCResourceLoaders;
import uk.nbforge.gallifreyan_chronicles.network.GCFabricNetworking;

public class GallifreyanChroniclesFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        GameRuleCategory.register(GCGameRuleCategories.GALLIFREYAN_CHRONICLES.id());
        GCFabricCommands.registerCommands();
        GCEntityTypes.registerEntityTypes();
        GCBlockEntities.registerBlockEntities();
        GCEvents.registerEvents();
        GCCreativeModeTabs.registerFabricCreativeModeTabs();
        GCFabricWorldGeneration.registerWorldGeneration();
        GCFabricNetworking.registerNetworking();
        GCResourceLoaders.registerResourceLoaders();
        CommonClass.init();
        ItemComponentTooltipProviderRegistry.addLast(GCDataComponents.ITEM_INFO);
    }
}

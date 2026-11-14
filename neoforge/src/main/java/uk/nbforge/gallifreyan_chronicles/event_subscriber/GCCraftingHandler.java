package uk.nbforge.gallifreyan_chronicles.event_subscriber;

import uk.nbforge.gallifreyan_chronicles.Constants;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import uk.nbforge.gallifreyan_chronicles.core.GCDataComponents;
import uk.nbforge.gallifreyan_chronicles.core.GCBlocks;

public class GCCraftingHandler {
    @SubscribeEvent
    public void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        ItemStack crafted = event.getCrafting();

        if (crafted.getItem() == Items.STONE_PICKAXE) {
            boolean hasGallifreyan = false;

            for (int i = 0; i < event.getInventory().getContainerSize(); i++) {
                ItemStack stack = event.getInventory().getItem(i);
                if (!stack.isEmpty() && stack.getItem() == GCBlocks.GALLIFREYAN_COBBLESTONE.asItem()) {
                    hasGallifreyan = true;
                    break;
                }
            }

            if (hasGallifreyan) {
                crafted.set(GCDataComponents.COBBLESTONE_VARIANT, "gallifreyan");
                Constants.LOG.info("Set gallifreyan variant on stone pickaxe");
            }
        }
    }
}
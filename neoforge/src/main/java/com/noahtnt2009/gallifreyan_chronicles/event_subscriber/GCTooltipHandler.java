package com.noahtnt2009.gallifreyan_chronicles.event_subscriber;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.init.GCDataComponents;
import com.noahtnt2009.gallifreyan_chronicles.item.ItemInfoTooltip;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public final class GCTooltipHandler {
    private GCTooltipHandler() {
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        ItemInfoTooltip info = stack.get(GCDataComponents.ITEM_INFO);
        if (info == null) return;

        info.addToTooltip(event.getContext(), event.getToolTip()::add, event.getFlags(), stack.getComponents());
    }
}

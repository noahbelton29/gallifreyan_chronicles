package com.noahtnt2009.gallifreyan_chronicles.event_subscriber;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.client.renderer.OutlineHider;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ExtractBlockOutlineRenderStateEvent;

@SuppressWarnings("unused")
@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public class NeoForgeOutlineHider {
    @SubscribeEvent
    public static void onBlockOutline(ExtractBlockOutlineRenderStateEvent event) {
        if (OutlineHider.shouldHide(event.getLevel(), event.getBlockPos())) {
            event.setCanceled(true);
        }
    }
}
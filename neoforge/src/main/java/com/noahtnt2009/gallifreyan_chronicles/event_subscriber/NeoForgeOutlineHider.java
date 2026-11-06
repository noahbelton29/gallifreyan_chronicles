//package com.noahtnt2009.gallifreyan_chronicles.event_subscriber;
//
//import com.noahtnt2009.gallifreyan_chronicles.Constants;
//import com.noahtnt2009.gallifreyan_chronicles.client.renderer.OutlineHider;
//import net.neoforged.api.distmarker.Dist;
//import net.neoforged.bus.api.SubscribeEvent;
//import net.neoforged.fml.common.EventBusSubscriber;
//
//@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
//public class NeoForgeOutlineHider {
//    @SubscribeEvent
//    public static void onBlockHighlight(RenderHighlightEvent.Block event) {
//        if (OutlineHider.shouldHide(event.getTarget().getBlockPos())) {
//            event.setCanceled(true);
//        }
//    }
//}
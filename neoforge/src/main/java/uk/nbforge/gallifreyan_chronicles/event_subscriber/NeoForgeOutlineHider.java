package uk.nbforge.gallifreyan_chronicles.event_subscriber;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ExtractBlockOutlineRenderStateEvent;
import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.client.renderer.util.OutlineHider;

@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public class NeoForgeOutlineHider {
    @SubscribeEvent
    public static void onBlockOutline(ExtractBlockOutlineRenderStateEvent event) {
        if (OutlineHider.shouldHide(event.getLevel(), event.getBlockPos())) {
            event.setCanceled(true);
        }
    }
}

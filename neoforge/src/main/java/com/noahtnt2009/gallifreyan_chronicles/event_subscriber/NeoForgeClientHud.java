package com.noahtnt2009.gallifreyan_chronicles.event_subscriber;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.client.gui.hud.GCHudRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public class NeoForgeClientHud {

    @SubscribeEvent
    public static void renderBelowHud(RenderGuiEvent.Pre event) {
        GCHudRenderer.renderBelowHud(event.getGuiGraphics());
    }

    @SubscribeEvent
    public static void renderHud(RenderGuiEvent.Post event) {
        GCHudRenderer.render(event.getGuiGraphics());
    }
}

package com.noahtnt2009.gallifreyan_chronicles.event_subscriber;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisConsoleBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisExteriorBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.client.renderer.TardisConsoleBlockRenderer;
import com.noahtnt2009.gallifreyan_chronicles.client.renderer.TardisControlEntityRenderer;
import com.noahtnt2009.gallifreyan_chronicles.client.renderer.TardisExteriorBlockRenderer;
import com.noahtnt2009.gallifreyan_chronicles.init.GCEntityTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public class NeoForgeClientRenderers {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(TardisExteriorBlockEntity.TYPE.get(), TardisExteriorBlockRenderer::new);
        event.registerBlockEntityRenderer(TardisConsoleBlockEntity.TYPE.get(), TardisConsoleBlockRenderer::new);
        event.registerEntityRenderer(GCEntityTypes.TARDIS_CONTROL_ENTITY_TYPE.get(), TardisControlEntityRenderer::new);

        Constants.LOG.info("Registered GC Client Renderers (NeoForge)");
    }
}
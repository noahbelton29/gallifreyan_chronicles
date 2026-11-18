package uk.nbforge.gallifreyan_chronicles.event_subscriber;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.client.renderer.block.InteriorDoorBlockRenderer;
import uk.nbforge.gallifreyan_chronicles.client.renderer.block.TardisConsoleBlockRenderer;
import uk.nbforge.gallifreyan_chronicles.client.renderer.block.TardisExteriorBlockRenderer;
import uk.nbforge.gallifreyan_chronicles.client.renderer.entity.TardisControlEntityRenderer;
import uk.nbforge.gallifreyan_chronicles.client.renderer.entity.TardisKeyEntityRenderer;
import uk.nbforge.gallifreyan_chronicles.core.GCEntityTypes;
import uk.nbforge.gallifreyan_chronicles.core.block_entity.InteriorDoorBlockEntity;
import uk.nbforge.gallifreyan_chronicles.core.block_entity.TardisConsoleBlockEntity;
import uk.nbforge.gallifreyan_chronicles.core.block_entity.TardisExteriorBlockEntity;

@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public class NeoForgeClientRenderers {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(TardisExteriorBlockEntity.TYPE.get(), TardisExteriorBlockRenderer::new);
        event.registerBlockEntityRenderer(TardisConsoleBlockEntity.TYPE.get(), TardisConsoleBlockRenderer::new);
        event.registerBlockEntityRenderer(InteriorDoorBlockEntity.TYPE.get(), InteriorDoorBlockRenderer::new);
        event.registerEntityRenderer(GCEntityTypes.TARDIS_CONTROL_ENTITY_TYPE.get(), TardisControlEntityRenderer::new);
        event.registerEntityRenderer(GCEntityTypes.TARDIS_KEY_ENTITY_TYPE.get(), TardisKeyEntityRenderer::new);

        Constants.LOG.info("Registered GC Client Renderers (NeoForge)");
    }
}

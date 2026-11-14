package uk.nbforge.gallifreyan_chronicles.client.renderer.entity;

import uk.nbforge.gallifreyan_chronicles.core.entity.TardisControlEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.jspecify.annotations.NonNull;

public class TardisControlEntityRenderer extends EntityRenderer<TardisControlEntity, EntityRenderState> {
    public TardisControlEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public @NonNull EntityRenderState createRenderState() {
        return new EntityRenderState();
    }
}
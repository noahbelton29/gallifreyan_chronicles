package uk.nbforge.gallifreyan_chronicles.client.renderer.entity;

import uk.nbforge.gallifreyan_chronicles.core.entity.TardisKeyEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.jspecify.annotations.NonNull;

public class TardisKeyEntityRenderer extends EntityRenderer<TardisKeyEntity, EntityRenderState> {
    public TardisKeyEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public @NonNull EntityRenderState createRenderState() {
        return new EntityRenderState();
    }
}

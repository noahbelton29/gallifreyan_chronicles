package uk.nbforge.gallifreyan_chronicles.client.model;

import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;
import uk.nbforge.gallifreyan_chronicles.client.renderer.block.TardisExteriorBlockRenderer;
import uk.nbforge.gallifreyan_chronicles.core.block_entity.TardisExteriorBlockEntity;
import uk.nbforge.gallifreyan_chronicles.core.data_registry.TardisExteriorRegistry;

public class TardisExteriorBlockModel extends GeoModel<TardisExteriorBlockEntity> {
    @Override
    public @NonNull Identifier getModelResource(@NonNull GeoRenderState renderState) {
        return GeoAssets.model(exteriorId(renderState));
    }

    @Override
    public @NonNull Identifier getTextureResource(@NonNull GeoRenderState renderState) {
        return GeoAssets.texture(exteriorId(renderState));
    }

    @Override
    public @NonNull Identifier getAnimationResource(TardisExteriorBlockEntity animatable) {
        return GeoAssets.EXTERIOR.animation(animatable.getExterior().id());
    }

    private static String exteriorId(GeoRenderState renderState) {
        String id = GeoAssets.read(renderState, TardisExteriorBlockRenderer.EXTERIOR_ID);
        return id != null && TardisExteriorRegistry.contains(id)
                ? id
                : TardisExteriorRegistry.getDefault().id();
    }
}

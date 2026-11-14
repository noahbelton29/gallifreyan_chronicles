package uk.nbforge.gallifreyan_chronicles.client.model;

import uk.nbforge.gallifreyan_chronicles.client.model.GeoAssets;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import uk.nbforge.gallifreyan_chronicles.core.block_entities.TardisExteriorBlockEntity;
import uk.nbforge.gallifreyan_chronicles.client.renderer.block.TardisExteriorBlockRenderer;
import uk.nbforge.gallifreyan_chronicles.core.dynamic_registries.TardisExteriorRegistry;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public class TardisExteriorBlockModel extends GeoModel<TardisExteriorBlockEntity> {
    @Override
    public @NonNull Identifier getModelResource(@NonNull GeoRenderState renderState) {
        return GeoAssets.EXTERIOR.model(getExteriorId(renderState));
    }

    @Override
    public @NonNull Identifier getTextureResource(@NonNull GeoRenderState renderState) {
        return GeoAssets.EXTERIOR.texture(getExteriorId(renderState));
    }

    @Override
    public @NonNull Identifier getAnimationResource(TardisExteriorBlockEntity animatable) {
        return GeoAssets.EXTERIOR.animation(animatable.getExterior().id());
    }

    private String getExteriorId(GeoRenderState renderState) {
        if (renderState.hasGeckolibData(TardisExteriorBlockRenderer.EXTERIOR_ID)) {
            String id = renderState.getGeckolibData(TardisExteriorBlockRenderer.EXTERIOR_ID);
            if (TardisExteriorRegistry.contains(id)) {
                return id;
            }
        }
        return TardisExteriorRegistry.getDefault().id();
    }
}

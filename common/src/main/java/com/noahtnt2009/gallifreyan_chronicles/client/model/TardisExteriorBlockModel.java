package com.noahtnt2009.gallifreyan_chronicles.client.model;

import com.noahtnt2009.gallifreyan_chronicles.client.tardis.GeoAssets;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisExteriorBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.client.renderer.TardisExteriorBlockRenderer;
import com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.TardisExteriorRegistry;
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

package com.noahtnt2009.gallifreyan_chronicles.block;

import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisExteriorBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.client.renderer.TardisExteriorBlockRenderer;
import com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.TardisExterior;
import com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.TardisExteriorRegistry;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public class TardisExteriorBlockModel extends GeoModel<TardisExteriorBlockEntity> {

    @Override
    public @NonNull Identifier getModelResource(GeoRenderState renderState) {
        return getExterior(renderState).model();
    }

    @Override
    public @NonNull Identifier getTextureResource(GeoRenderState renderState) {
        return getExterior(renderState).texture();
    }

    @Override
    public @NonNull Identifier getAnimationResource(TardisExteriorBlockEntity animatable) {
        Identifier id = animatable.getExterior().animation();
        System.out.println("Animation ID: " + id);
        return id;
    }

    private TardisExterior getExterior(GeoRenderState renderState) {
        if (renderState.hasGeckolibData(TardisExteriorBlockRenderer.EXTERIOR_ID)) {
            String id = renderState.getGeckolibData(TardisExteriorBlockRenderer.EXTERIOR_ID);
            return TardisExteriorRegistry.get(id);
        }
        return TardisExteriorRegistry.getDefault();
    }
}
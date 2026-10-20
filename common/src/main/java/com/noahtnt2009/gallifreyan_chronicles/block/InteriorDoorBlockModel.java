package com.noahtnt2009.gallifreyan_chronicles.block;

import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.InteriorDoorBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.client.renderer.InteriorDoorBlockRenderer;
import com.noahtnt2009.gallifreyan_chronicles.client.tardis.InteriorDoorAssets;
import com.noahtnt2009.gallifreyan_chronicles.util.GCUtils;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public class InteriorDoorBlockModel extends GeoModel<InteriorDoorBlockEntity> {
    private static final String DEFAULT_VARIANT = "brachacki_doors";

    @Override
    public @NonNull Identifier getModelResource(@NonNull GeoRenderState renderState) {
        return InteriorDoorAssets.model(getVariantId(renderState));
    }

    @Override
    public @NonNull Identifier getTextureResource(@NonNull GeoRenderState renderState) {
        return InteriorDoorAssets.texture(getVariantId(renderState));
    }

    @Override
    public @NonNull Identifier getAnimationResource(InteriorDoorBlockEntity animatable) {
        return InteriorDoorAssets.animation(GCUtils.of(animatable.getVariant()));
    }

    private Identifier getVariantId(GeoRenderState renderState) {
        if (renderState.hasGeckolibData(InteriorDoorBlockRenderer.VARIANT)) {
            String id = renderState.getGeckolibData(InteriorDoorBlockRenderer.VARIANT);
            if (id != null) {
                return GCUtils.of(id);
            }
        }
        return GCUtils.of(DEFAULT_VARIANT);
    }
}

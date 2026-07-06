package com.noahtnt2009.gallifreyan_chronicles.client.renderer.geo_layer;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.renderer.layer.builtin.AutoGlowingGeoLayer;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.noahtnt2009.gallifreyan_chronicles.client.renderer.TardisExteriorBlockRenderer;
import com.noahtnt2009.gallifreyan_chronicles.util.GCUtils;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public class TardisExteriorGlowLayer<T extends GeoAnimatable, O, R extends GeoRenderState>
        extends AutoGlowingGeoLayer<T, O, R> {

    public TardisExteriorGlowLayer(GeoRenderer<T, O, R> renderer) {
        super(renderer);
    }

    @Override
    protected RenderType getRenderType(@NonNull R renderState) {
        boolean glowing = renderState.getOrDefaultGeckolibData(TardisExteriorBlockRenderer.GLOWING, false);

        if (!glowing) return null;
        return super.getRenderType(renderState);
    }

    @Override
    protected @NonNull Identifier getTextureResource(@NonNull R renderState) {
        Identifier base = this.renderer.getTextureLocation(renderState);
        String path = base.getPath().replace(".png", "_glowmask.png");
        return GCUtils.of(path);
    }
}

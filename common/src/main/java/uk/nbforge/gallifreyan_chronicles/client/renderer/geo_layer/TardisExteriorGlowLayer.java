package uk.nbforge.gallifreyan_chronicles.client.renderer.geo_layer;

import uk.nbforge.gallifreyan_chronicles.client.renderer.fade.FadeBoxRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.constant.DataTickets;
import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.renderer.layer.builtin.AutoGlowingGeoLayer;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import uk.nbforge.gallifreyan_chronicles.client.renderer.GCRenderTypes;
import uk.nbforge.gallifreyan_chronicles.client.renderer.block.TardisExteriorBlockRenderer;
import uk.nbforge.gallifreyan_chronicles.util.GCUtil;
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
        if (renderState.getOrDefaultGeckolibData(TardisExteriorBlockRenderer.LAMP, 1.0f) <= 0.01f) return null;
        if (renderState.getOrDefaultGeckolibData(TardisExteriorBlockRenderer.OPACITY, 1.0f) <= 0.0f) return null;
        /* while fading on the canvas, the glow goes on the canvas too so it fades with the box */
        boolean fading = renderState.getOrDefaultGeckolibData(TardisExteriorBlockRenderer.OPACITY, 1.0f) < 1.0f;
        if (fading && FadeBoxRenderer.active()) return FadeBoxRenderer.glowRenderType(getTextureResource(renderState));
        return GCRenderTypes.layeredEyes(getTextureResource(renderState));
    }

    /* dims the glow by the lamp brightness, so it can flicker and fade */
    @Override
    public void submitRenderTask(@NonNull RenderPassInfo<R> info, @NonNull SubmitNodeCollector tasks) {
        R state = info.renderState();
        float lamp = state.getOrDefaultGeckolibData(TardisExteriorBlockRenderer.LAMP, 1.0f);
        if (lamp >= 1.0f) {
            super.submitRenderTask(info, tasks);
            return;
        }

        int original = info.renderColor();
        int alpha = Math.round(((original >>> 24) & 0xFF) * Math.clamp(lamp, 0f, 1f));
        state.addGeckolibData(DataTickets.RENDER_COLOR, (alpha << 24) | (original & 0x00FFFFFF));
        try {
            super.submitRenderTask(info, tasks);
        } finally {
            state.addGeckolibData(DataTickets.RENDER_COLOR, original);
        }
    }

    @Override
    protected @NonNull Identifier getTextureResource(@NonNull R renderState) {
        Identifier base = this.renderer.getTextureLocation(renderState);
        String path = base.getPath().replace(".png", "_glowmask.png");
        return GCUtil.modIdentifier(path);
    }
}
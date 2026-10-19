
package com.noahtnt2009.gallifreyan_chronicles.client.renderer.geo_layer;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.constant.DataTickets;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.builtin.AutoGlowingGeoLayer;
import com.noahtnt2009.gallifreyan_chronicles.client.renderer.TardisConsoleBlockRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public class TardisConsoleFadeGlowLayer<T extends GeoAnimatable, O, R extends GeoRenderState>
        extends AutoGlowingGeoLayer<T, O, R> {

    private static final float MIN_WEIGHT = 0.01f;
    private static final float FADE_POWER = 2.5f;
    private static final float HOLD_TICKS = 30f; // 1.5 seconds
    private static final float FADE_TICKS = 8f;  // Fast 0.2-second fade

    private final int index;
    private final int count;
    private final float stepTicks;

    public TardisConsoleFadeGlowLayer(
            GeoRenderer<T, O, R> renderer,
            int index,
            int count,
            float stepTicks) {
        super(renderer);

        if (count <= 0 || index < 0 || index >= count || stepTicks <= 0)
            throw new IllegalArgumentException("Invalid glow layer parameters");

        this.index = index;
        this.count = count;
        this.stepTicks = stepTicks;
    }

    private float weight(R renderState) {
        Float time = renderState.getOrDefaultGeckolibData(
                TardisConsoleBlockRenderer.GLOW_TIME, 0f);

        float elapsed = time == null ? 0f : time;

        // Each layer gets a 1.5-second hold and a quick fade.
        float cycleTicks = HOLD_TICKS + FADE_TICKS;
        float cycle = elapsed / cycleTicks;
        float localTime = elapsed % cycleTicks;

        // Which layer is active?
        int activeIndex = (int) Math.floor(cycle) % count;

        // Hold the current layer at full brightness.
        if (localTime < HOLD_TICKS) {
            return index == activeIndex ? 1f : 0f;
        }

        // Fade out the current layer and fade in the next.
        float fade = (localTime - HOLD_TICKS) / FADE_TICKS;
        fade = Math.clamp(fade, 0f, 1f);

        if (index == activeIndex) {
            return (float) Math.pow(1f - fade, FADE_POWER);
        }

        if (index == (activeIndex + 1) % count) {
            return (float) Math.pow(fade, FADE_POWER);
        }

        return 0f;
    }

    @Override
    protected @NonNull Identifier getTextureResource(@NonNull R renderState) {
        Identifier base = this.renderer.getTextureLocation(renderState);

        String path = base.getPath().replace(
                ".png", "_glowmask_" + index + ".png");

        return Identifier.fromNamespaceAndPath(
                base.getNamespace(), path);
    }

    @Override
    protected RenderType getRenderType(@NonNull R renderState) {
        if (weight(renderState) < MIN_WEIGHT)
            return null;

        return super.getRenderType(renderState);
    }

    @Override
    public void submitRenderTask(
            @NonNull RenderPassInfo<R> info,
            @NonNull SubmitNodeCollector tasks) {

        R state = info.renderState();
        float w = weight(state);

        if (w < MIN_WEIGHT)
            return;

        int original = info.renderColor();

        // Preserve white RGB and fade through alpha.
        int alpha = Math.round(w * 255f);
        int argb = (alpha << 24) | 0x00FFFFFF;

        state.addGeckolibData(DataTickets.RENDER_COLOR, argb);

        try {
            super.submitRenderTask(info, tasks);
        } finally {
            state.addGeckolibData(DataTickets.RENDER_COLOR, original);
        }
    }
}

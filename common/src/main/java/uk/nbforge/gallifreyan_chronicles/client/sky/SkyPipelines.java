package uk.nbforge.gallifreyan_chronicles.client.sky;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.renderpearl.api.pipeline.BlendFactor;
import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.CompareOp;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import uk.nbforge.gallifreyan_chronicles.util.GCUtil;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.RenderPipelines;

import java.util.Optional;

public final class SkyPipelines {
    private static final DepthStencilState BEHIND_SCENE = new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, false);

    public static final RenderPipeline BODY = texturedBody("sky_body")
            .withDepthStencilState(Optional.empty())
            .build();

    public static final RenderPipeline OVERLAY_BACKDROP = stars("sky_overlay_backdrop")
            .withColorTargetState(ColorTargetState.DEFAULT)
            .withCull(false)
            .withDepthStencilState(BEHIND_SCENE)
            .build();

    public static final RenderPipeline OVERLAY_FOG = stars("sky_overlay_fog")
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withCull(false)
            .withDepthStencilState(BEHIND_SCENE)
            .build();

    public static final RenderPipeline OVERLAY_STARS = stars("sky_overlay_stars")
            .withColorTargetState(new ColorTargetState(BlendFunction.OVERLAY))
            .withDepthStencilState(BEHIND_SCENE)
            .build();

    public static final RenderPipeline OVERLAY_BODY = texturedBody("sky_overlay_body")
            .withDepthStencilState(BEHIND_SCENE)
            .build();

    /* stars add light but leave the canvas see-through */
    private static final BlendFunction ADD_KEEP_ALPHA = new BlendFunction(BlendFactor.SRC_ALPHA, BlendFactor.ONE, BlendFactor.ZERO, BlendFactor.ONE);

    public static final RenderPipeline CANVAS_STARS = stars("sky_canvas_stars")
            .withColorTargetState(new ColorTargetState(ADD_KEEP_ALPHA))
            .withDepthStencilState(BEHIND_SCENE)
            .build();

    /* lays the sky canvas over the screen after a shader pack is done */
    public static final RenderPipeline CANVAS_COMPOSITE = RenderPipeline.builder(RenderPipelines.GLOBALS_SNIPPET)
            .withLocation(GCUtil.modIdentifier("pipeline/sky_canvas_composite"))
            .withVertexShader("core/screenquad")
            .withFragmentShader("core/blit_screen")
            .withBindGroupLayout(BindGroupLayouts.IN_SAMPLER)
            .withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT_PREMULTIPLIED_ALPHA))
            .build();

    private static RenderPipeline.Builder sky(String name) {
        return RenderPipeline.builder(RenderPipelines.GLOBALS_SNIPPET)
                .withBindGroupLayout(BindGroupLayouts.PROJECTION)
                .withBindGroupLayout(BindGroupLayouts.DYNAMIC_TRANSFORMS)
                .withLocation(GCUtil.modIdentifier("pipeline/" + name))
                .withPrimitiveTopology(PrimitiveTopology.QUADS);
    }

    private static RenderPipeline.Builder texturedBody(String name) {
        return sky(name)
                .withVertexShader("core/position_tex")
                .withFragmentShader("core/position_tex")
                .withBindGroupLayout(BindGroupLayouts.SAMPLER0)
                .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX)
                .withCull(false);
    }

    private static RenderPipeline.Builder stars(String name) {
        return sky(name)
                .withVertexShader("core/stars")
                .withFragmentShader("core/stars")
                .withVertexBinding(0, DefaultVertexFormat.POSITION);
    }

    private SkyPipelines() {
    }
}

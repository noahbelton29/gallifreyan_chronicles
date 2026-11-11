package com.noahtnt2009.gallifreyan_chronicles.client.sky;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.CompareOp;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.noahtnt2009.gallifreyan_chronicles.util.GCUtils;
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

    private static RenderPipeline.Builder sky(String name) {
        return RenderPipeline.builder(RenderPipelines.GLOBALS_SNIPPET)
                .withBindGroupLayout(BindGroupLayouts.PROJECTION)
                .withBindGroupLayout(BindGroupLayouts.DYNAMIC_TRANSFORMS)
                .withLocation(GCUtils.modIdentifier("pipeline/" + name))
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

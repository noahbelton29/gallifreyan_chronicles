package uk.nbforge.gallifreyan_chronicles.client.renderer;

import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.BindGroupLayouts;
import uk.nbforge.gallifreyan_chronicles.util.GCUtil;
import com.mojang.renderpearl.api.pipeline.CompareOp;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

public final class GCRenderTypes {
    private static final Map<Identifier, RenderType> LAYERED_EYES = new HashMap<>();

    private GCRenderTypes() {
    }

    private static final Map<Identifier, RenderType> GLOW_ORB = new HashMap<>();

    /* the lamp orb: adds light so it glows with no dark edges, and doesn't write depth, so it never hides the sky
       or the box behind it */
    /* same setup as vanilla's eyes pipeline (those snippets are private so it's copied here) */
    private static final RenderPipeline GLOW_ORB_PIPELINE = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.GLOBALS_SNIPPET)
                    .withBindGroupLayout(BindGroupLayouts.PROJECTION)
                    .withBindGroupLayout(BindGroupLayouts.DYNAMIC_TRANSFORMS)
                    .withBindGroupLayout(BindGroupLayouts.FOG)
                    .withBindGroupLayout(BindGroupLayouts.SAMPLER0)
                    .withVertexShader("core/entity")
                    .withFragmentShader("core/entity")
                    .withVertexBinding(0, DefaultVertexFormat.ENTITY)
                    .withPrimitiveTopology(PrimitiveTopology.QUADS)
                    .withShaderDefine("EMISSIVE")
                    .withShaderDefine("NO_OVERLAY")
                    .withShaderDefine("NO_CARDINAL_LIGHTING")
                    .withLocation(GCUtil.modIdentifier("pipeline/glow_orb"))
                    .withColorTargetState(new ColorTargetState(BlendFunction.LIGHTNING))
                    .withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, false))
                    .withCull(false)
                    .build());

    public static RenderType glowOrb(Identifier texture) {
        return GLOW_ORB.computeIfAbsent(texture, t -> RenderType.create(
                "gc_glow_orb",
                RenderSetup.builder(GLOW_ORB_PIPELINE)
                        .withTexture("Sampler0", t)
                        .sortOnUpload()
                        .createRenderSetup()));
    }

    public static RenderType layeredEyes(Identifier texture) {
        return LAYERED_EYES.computeIfAbsent(texture, t -> RenderType.create(
                "gc_layered_eyes",
                RenderSetup.builder(RenderPipelines.EYES)
                        .withTexture("Sampler0", t)
                        .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                        .sortOnUpload()
                        .createRenderSetup()));
    }
}
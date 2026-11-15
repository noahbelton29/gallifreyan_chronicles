package uk.nbforge.gallifreyan_chronicles.client.renderer.fade;

import com.mojang.renderpearl.api.pipeline.CompareOp;
import uk.nbforge.gallifreyan_chronicles.Constants;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.commands.RenderPassDescriptor;
import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.textures.FilterMode;
import uk.nbforge.gallifreyan_chronicles.client.sky.SkyShaderCompat;
import uk.nbforge.gallifreyan_chronicles.compat.portal.TardisPortalBridge;
import uk.nbforge.gallifreyan_chronicles.util.GCUtil;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.StagedVertexBuffer;
import net.minecraft.client.renderer.rendertype.PreparedRenderType;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import org.joml.Vector4f;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;

/**
   Draws a fading TARDIS on its own canvas, then lays the canvas over the world.
   The box only hides its own back faces, so water and clouds still draw.
   Each frame goes like this.
   One, the box is drawn with a special render type and we hold it instead of drawing it.
   Two, after the world is drawn we copy the world depth into our canvas, so walls in front still hide the box.
   Three, we draw the held boxes into the canvas, solid and keeping their faded alpha.
   Four, we lay the canvas over the screen.
*/
public final class FadeBoxRenderer {
    private static final String NAME = "gc_fading_box";
    // The glowmask is held while it fades so it fades with the box
    private static final String GLOW_NAME = "gc_fading_glow";
    // The lamp orb is held too so it can go on top of the box
    private static final String ORB_NAME = "gc_glow_orb";

    // The box, solid so only the nearest face wins, lit like any entity
    private static final RenderPipeline BOX_PIPELINE = RenderPipelines.register(
            boxPipeline(DefaultVertexFormat.ENTITY, "pipeline/fading_box"));

    private static final RenderPipeline GLOW_PIPELINE = RenderPipelines.register(
            glowPipeline(DefaultVertexFormat.ENTITY, "pipeline/fading_glow"));

    // Shader packs make entity vertices bigger so we read things with their real format
    // One pipeline per kind and format
    private static final Map<String, RenderPipeline> FORMAT_PIPELINES = new HashMap<>();
    private static boolean irisFormatLooked;
    private static @Nullable VertexFormat irisFormat;

    private static RenderPipeline boxPipeline(VertexFormat format, String location) {
        return RenderPipeline.builder(RenderPipelines.GLOBALS_SNIPPET)
                    .withBindGroupLayout(BindGroupLayouts.PROJECTION)
                    .withBindGroupLayout(BindGroupLayouts.DYNAMIC_TRANSFORMS)
                    .withBindGroupLayout(BindGroupLayouts.FOG)
                    .withBindGroupLayout(BindGroupLayouts.LIGHTING)
                    .withBindGroupLayout(BindGroupLayouts.SAMPLER0)
                    .withBindGroupLayout(BindGroupLayouts.SAMPLER1)
                    .withBindGroupLayout(BindGroupLayouts.SAMPLER2)
                    .withVertexShader("core/entity")
                    .withFragmentShader("core/entity")
                    .withVertexBinding(0, format)
                    .withPrimitiveTopology(PrimitiveTopology.QUADS)
                    .withShaderDefine("ALPHA_CUTOUT", 0.01F)
                    .withLocation(GCUtil.modIdentifier(location))
                    .withColorTargetState(ColorTargetState.DEFAULT)
                    .withDepthStencilState(DepthStencilState.DEFAULT)
                    .build();
    }

    /** Glows like the eyes in vanilla, no lighting, see through and does not hide what is behind it. */
    private static RenderPipeline glowPipeline(VertexFormat format, String location) {
        return glowLike(format, location, BlendFunction.TRANSLUCENT, true);
    }

    /** The lamp orb, it adds light and is never culled. */
    private static RenderPipeline orbPipeline(VertexFormat format, String location) {
        return glowLike(format, location, BlendFunction.LIGHTNING, false);
    }

    private static RenderPipeline glowLike(VertexFormat format, String location, BlendFunction blend, boolean cull) {
        return RenderPipeline.builder(RenderPipelines.GLOBALS_SNIPPET)
                .withBindGroupLayout(BindGroupLayouts.PROJECTION)
                .withBindGroupLayout(BindGroupLayouts.DYNAMIC_TRANSFORMS)
                .withBindGroupLayout(BindGroupLayouts.FOG)
                .withBindGroupLayout(BindGroupLayouts.SAMPLER0)
                .withVertexShader("core/entity")
                .withFragmentShader("core/entity")
                .withVertexBinding(0, format)
                .withPrimitiveTopology(PrimitiveTopology.QUADS)
                .withShaderDefine("EMISSIVE")
                .withShaderDefine("NO_OVERLAY")
                .withShaderDefine("NO_CARDINAL_LIGHTING")
                .withLocation(GCUtil.modIdentifier(location))
                .withColorTargetState(new ColorTargetState(blend))
                .withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, false))
                .withCull(cull)
                .build();
    }

    private interface PipelineMaker {
        RenderPipeline make(VertexFormat format, String location);
    }

    /** The pipeline that matches the format things were really written in. */
    private static RenderPipeline pipelineNow(String kind, RenderPipeline vanilla, PipelineMaker maker) {
        VertexFormat format = writtenFormat();
        if (format.equals(DefaultVertexFormat.ENTITY)) return vanilla;
        return FORMAT_PIPELINES.computeIfAbsent(kind + format.hashCode(), key -> RenderPipelines.register(
                maker.make(format, "pipeline/fading_" + kind + "_shaders_" + Integer.toHexString(format.hashCode()))));
    }

    private static RenderPipeline orbPipelineNow(RenderPipeline vanilla) {
        return pipelineNow("orb", vanilla, FadeBoxRenderer::orbPipeline);
    }

    private static RenderPipeline boxPipelineNow() {
        return pipelineNow("box", BOX_PIPELINE, FadeBoxRenderer::boxPipeline);
    }

    private static VertexFormat writtenFormat() {
        if (SkyShaderCompat.isShaderPackInUse()) {
            VertexFormat iris = irisEntityFormat();
            if (iris != null) return iris;
        }
        return DefaultVertexFormat.ENTITY;
    }

    /** The bigger entity format from Iris, looked up by name so the mod works without Iris. */
    private static @Nullable VertexFormat irisEntityFormat() {
        if (!irisFormatLooked) {
            irisFormatLooked = true;
            try {
                Object value = Class.forName("net.irisshaders.iris.vertices.IrisVertexFormats").getField("ENTITY").get(null);
                if (value instanceof VertexFormat format) irisFormat = format;
            } catch (ReflectiveOperationException | LinkageError e) {
                Constants.LOG.warn("Could not find the Iris entity vertex format, the fading TARDIS may look wrong with shaders", e);
            }
        }
        return irisFormat;
    }

    // Lays the canvas over the screen using the alpha of each pixel
    private static final RenderPipeline COMPOSITE_PIPELINE = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.GLOBALS_SNIPPET)
                    .withLocation(GCUtil.modIdentifier("pipeline/fading_box_composite"))
                    .withVertexShader("core/screenquad")
                    .withFragmentShader("core/blit_screen")
                    .withBindGroupLayout(BindGroupLayouts.IN_SAMPLER)
                    .withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
                    .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                    .build());

    // The orb canvas is glow so it is added on top as light
    private static final RenderPipeline ORB_COMPOSITE_PIPELINE = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.GLOBALS_SNIPPET)
                    .withLocation(GCUtil.modIdentifier("pipeline/lamp_orb_composite"))
                    .withVertexShader("core/screenquad")
                    .withFragmentShader("core/blit_screen")
                    .withBindGroupLayout(BindGroupLayouts.IN_SAMPLER)
                    .withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
                    .withColorTargetState(new ColorTargetState(BlendFunction.ADDITIVE))
                    .build());

    private static final Map<Identifier, RenderType> TYPES = new HashMap<>();
    private static final List<Held> HELD = new ArrayList<>();
    private static final List<Held> HELD_ORBS = new ArrayList<>();
    private static final List<Held> HELD_GLOWS = new ArrayList<>();
    private static final Map<Identifier, RenderType> GLOW_TYPES = new HashMap<>();
    private static @Nullable TextureTarget canvas;
    private static boolean drawingOurOwn;
    // With a shader pack the canvas waits until the pack finishes the frame
    private static boolean canvasWaiting;

    private record Held(PreparedRenderType type, StagedVertexBuffer.ExecuteInfo info) {
    }

    private FadeBoxRenderer() {
    }

    /**
       How the fading box is drawn.
       CANVAS uses its own canvas over the world, same look with and without shaders.
       MASK is the old depth mask way, only used for views through a portal.
    */
    public enum Mode {
        CANVAS, MASK
    }

    public static Mode mode() {
        if (TardisPortalBridge.isRenderingAnyPortal()) return Mode.MASK;
        return Mode.CANVAS;
    }

    public static boolean active() {
        return mode() == Mode.CANVAS;
    }

    public static RenderType renderType(Identifier texture) {
        return TYPES.computeIfAbsent(texture, t -> RenderType.create(NAME,
                RenderSetup.builder(BOX_PIPELINE)
                        .withTexture("Sampler0", t)
                        .useLightmap()
                        .useOverlay()
                        .createRenderSetup()));
    }

    public static RenderType glowRenderType(Identifier texture) {
        return GLOW_TYPES.computeIfAbsent(texture, t -> RenderType.create(GLOW_NAME,
                RenderSetup.builder(GLOW_PIPELINE)
                        .withTexture("Sampler0", t)
                        .createRenderSetup()));
    }

    /** Replaces the normal draw, true means we kept it for later. */
    public static boolean hold(PreparedRenderType type, StagedVertexBuffer.ExecuteInfo info) {
        // The Iris shadow pass is the world seen from the sun, never hold those
        if (drawingOurOwn || SkyShaderCompat.isRenderingShadowPass()) return false;
        if (NAME.equals(type.name())) {
            HELD.add(new Held(type, info));
            return true;
        }
        if (GLOW_NAME.equals(type.name())) {
            HELD_GLOWS.add(new Held(type, info));
            return true;
        }
        // Orbs are only held on frames that have a fading box to go under them
        if (ORB_NAME.equals(type.name()) && active()) {
            HELD_ORBS.add(new Held(type, info));
            return true;
        }
        return false;
    }

    public static void clear() {
        HELD.clear();
        HELD_ORBS.clear();
        HELD_GLOWS.clear();
        canvasWaiting = false;
        boxesDrawn = false;
        orbsDrawn = false;
    }

    /** After the world is drawn, draw the held boxes on the canvas and lay it over the screen. */
    public static void drawAndComposite(RenderTarget main) {
        if (main.getColorTextureView() == null || main.getDepthTextureView() == null) {
            clear();
            return;
        }
        if (HELD.isEmpty() && HELD_ORBS.isEmpty()) return;

        // Everything is drawn now while the camera projection is the world one
        // Only the finished canvases are laid on later
        TextureTarget target = canvasFor(main);
        copyWorldDepth(main, target);
        if (!HELD_ORBS.isEmpty()) drawOrbs(main, target);
        if (!HELD.isEmpty()) drawBoxes(target);

        // A shader pack would paint over the canvases so they wait until it is done
        if (SkyShaderCompat.isShaderPackInUse()) {
            canvasWaiting = true;
            return;
        }
        layCanvas(main);
    }

    /** After the last pass of the shader pack, the same point vanilla draws mob outlines. */
    public static void afterShaders(RenderTarget main) {
        if (!canvasWaiting) return;
        canvasWaiting = false;
        if (main.getColorTextureView() == null) {
            clear();
            return;
        }
        layCanvas(main);
    }

    private static boolean boxesDrawn;
    private static boolean orbsDrawn;

    /** The box canvas first, then the orbs on top so they are always in front. */
    private static void layCanvas(RenderTarget main) {
        if (boxesDrawn && canvas != null) composite(main, canvas, COMPOSITE_PIPELINE);
        if (orbsDrawn && orbCanvas != null) composite(main, orbCanvas, ORB_COMPOSITE_PIPELINE);
        boxesDrawn = false;
        orbsDrawn = false;
    }

    /** The world depth, so walls, water and clouds in front still hide the box and orb. */
    private static void copyWorldDepth(RenderTarget main, TextureTarget target) {
        var nearest = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST);
        RenderPassDescriptor depthCopy = RenderPassDescriptor.builder(() -> "Fading TARDIS depth")
                .withDepthAttachment(target.getDepthTextureView())
                .build();
        try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(depthCopy)) {
            RenderSystem.bindDefaultUniforms(pass);
            pass.setPipeline(RenderSystem.getCompiledPipeline(RenderPipelines.BLIT_DEPTH));
            pass.setUniform("InSampler", main.getDepthTextureView(), nearest);
            pass.draw(3, 1, 0, 0);
        }
    }

    /** The orbs on their own canvas, tested against the world only before the box goes in so the box can never cover them. */
    private static void drawOrbs(RenderTarget main, TextureTarget depthSource) {
        TextureTarget target = orbCanvasFor(main);
        drawingOurOwn = true;
        try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                () -> "TARDIS lamp orbs", target.getColorTextureView(), Optional.of(new Vector4f(0f)),
                depthSource.getDepthTextureView(), OptionalDouble.empty())) {
            RenderSystem.bindDefaultUniforms(pass);
            for (Held held : HELD_ORBS) {
                draw(held, pass, orbPipelineNow(held.type().pipeline()));
            }
        } finally {
            drawingOurOwn = false;
            HELD_ORBS.clear();
        }
        orbsDrawn = true;
    }

    /** The boxes and their glowmask on a clear canvas. */
    private static void drawBoxes(TextureTarget target) {
        drawingOurOwn = true;
        try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                () -> "Fading TARDIS", target.getColorTextureView(), Optional.of(new Vector4f(0f)),
                target.getDepthTextureView(), OptionalDouble.empty())) {
            RenderSystem.bindDefaultUniforms(pass);
            RenderPipeline pipeline = boxPipelineNow();
            for (Held held : HELD) {
                draw(held, pass, pipeline);
            }
            // The glowmask goes on the canvas too, on top of the faces of the box
            RenderPipeline glow = pipelineNow("glow", GLOW_PIPELINE, FadeBoxRenderer::glowPipeline);
            for (Held held : HELD_GLOWS) {
                draw(held, pass, glow);
            }
        } finally {
            drawingOurOwn = false;
            HELD.clear();
            HELD_GLOWS.clear();
        }
        boxesDrawn = true;
    }

    /** Same as the game draw but with our pipeline so the vertex format matches. */
    private static void draw(Held held, RenderPass pass, RenderPipeline pipeline) {
        PreparedRenderType type = held.type();
        StagedVertexBuffer.ExecuteInfo info = held.info();
        pass.setPipeline(RenderSystem.getCompiledPipeline(pipeline));
        RenderSystem.bindDefaultUniforms(pass);
        pass.setUniform("DynamicTransforms", type.dynamicTransforms());
        pass.setVertexBuffer(0, info.vertexBuffer().slice());
        for (PreparedRenderType.Texture texture : type.textures()) {
            pass.setUniform(texture.name(), texture.textureView(), texture.sampler());
        }
        // Draws without an index list share the one from the game
        // It can grow later in the frame after this draw was held, so look it up again now
        if (info.customIndexBuffer() == null) {
            RenderSystem.AutoStorageIndexBuffer shared = RenderSystem.getSequentialBuffer(info.topology());
            pass.setIndexBuffer(shared.getBuffer(info.indexCount()), shared.type());
        } else {
            pass.setIndexBuffer(info.customIndexBuffer(), info.indexType());
        }
        pass.drawIndexed(info.indexCount(), 1, info.firstIndex(), info.baseVertex(), 0);
    }

    private static void composite(RenderTarget main, TextureTarget source, RenderPipeline pipeline) {
        var nearest = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST);
        try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                () -> "Fading TARDIS composite", main.getColorTextureView(), Optional.empty(),
                null, OptionalDouble.empty())) {
            pass.setPipeline(RenderSystem.getCompiledPipeline(pipeline));
            RenderSystem.bindDefaultUniforms(pass);
            pass.setUniform("InSampler", source.getColorTextureView(), nearest);
            pass.draw(3, 1, 0, 0);
        }
    }

    private static @Nullable TextureTarget orbCanvas;

    private static TextureTarget orbCanvasFor(RenderTarget main) {
        if (orbCanvas == null) {
            orbCanvas = new TextureTarget("TARDIS lamp orbs", main.width, main.height, GpuFormat.RGBA8_UNORM, null);
        } else if (orbCanvas.width != main.width || orbCanvas.height != main.height) {
            orbCanvas.resize(main.width, main.height);
        }
        return orbCanvas;
    }

    private static TextureTarget canvasFor(RenderTarget main) {
        if (canvas == null) {
            canvas = new TextureTarget("Fading TARDIS", main.width, main.height, GpuFormat.RGBA8_UNORM, GpuFormat.D32_FLOAT);
        } else if (canvas.width != main.width || canvas.height != main.height) {
            canvas.resize(main.width, main.height);
        }
        return canvas;
    }
}

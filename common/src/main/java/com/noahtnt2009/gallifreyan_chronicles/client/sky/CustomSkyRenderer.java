package com.noahtnt2009.gallifreyan_chronicles.client.sky;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.config.GCConfig;
import com.noahtnt2009.gallifreyan_chronicles.sky.DimensionSky;
import com.noahtnt2009.gallifreyan_chronicles.sky.DimensionSkyRegistry;
import com.noahtnt2009.gallifreyan_chronicles.sky.HexColor;
import com.noahtnt2009.gallifreyan_chronicles.sky.OverlayFog;
import com.noahtnt2009.gallifreyan_chronicles.sky.ShaderSky;
import com.noahtnt2009.gallifreyan_chronicles.sky.ResolvedSkyBody;
import com.noahtnt2009.gallifreyan_chronicles.sky.SkyBody;
import com.noahtnt2009.gallifreyan_chronicles.sky.SkyOrbit;
import com.noahtnt2009.gallifreyan_chronicles.sky.SkyStars;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;

public final class CustomSkyRenderer {
    private static final float BODY_DISTANCE = 100.0F;
    private static final float DARK_DISC_OFFSET = 12.0F;
    private static final float GOLDEN_ANGLE = 2.3999632F;
    private static final float TWO_PI = (float) (Math.PI * 2.0);
    private static final float DAY_FADE = 0.12F;

    private static @Nullable StarDensity meshDensity;
    private static final Map<DimensionSky, SkyMeshes> MESHES = new IdentityHashMap<>();
    private static final Set<Identifier> MISSING_BODIES = new HashSet<>();
    private static @Nullable SkyRenderState overlayState;
    private static @Nullable GpuBufferSlice overlayFog;
    private static @Nullable ClientLevel overlayLevel;
    private static final float OVERLAY_DISTANCE = 0.5F;
    private static final float CUBE_DIAGONAL = 1.7320508F;
    private static final int FOG_SHELLS = 48;
    private static final float FOG_MAX_DISTANCE = 0.9F;

    private record PreparedBody(ResolvedSkyBody body, AbstractTexture texture) {
    }

    private CustomSkyRenderer() {
    }

    public static boolean render(RenderTarget target, GpuBufferSlice skyFog, SkyRenderState state) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return false;

        DimensionSky sky = DimensionSkyRegistry.getForDimension(level.dimension().identifier());
        if (sky == null) return false;

        SkyShaderCompat.assignPipelines();
        if (sky.shaderSky() == ShaderSky.OVERLAY && SkyShaderCompat.isShaderPackInUse()) {
            overlayState = state;
            overlayFog = skyFog;
            overlayLevel = level;
            return true;
        }

        SkyMeshes skyMeshes = meshesFor(sky);
        List<PreparedBody> bodies = prepareBodies(sky);

        RenderSystem.setShaderFog(skyFog);
        try (RenderPass pass = RenderSystem.getDevice()
                .createCommandEncoder()
                .createRenderPass(() -> "GC custom sky", target.getColorTextureView(), Optional.empty(),
                        target.getDepthTextureView(), OptionalDouble.empty())) {
            RenderSystem.bindDefaultUniforms(pass);

            SkyShaderCompat.setPhase(SkyShaderCompat.Phase.SKY);
            drawDisc(pass, skyMeshes.topDisc(), RenderSystem.getModelViewMatrixCopy(), skyColor(sky, state));
            SkyShaderCompat.setPhase(SkyShaderCompat.Phase.STARS);
            drawStars(pass, skyMeshes, sky, state.rainBrightness, RenderPipelines.STARS, 1.0F);
            SkyShaderCompat.setPhase(SkyShaderCompat.Phase.CUSTOM_SKY);
            float daylight = daylight(state.sunAngle);
            for (PreparedBody prepared : bodies) {
                drawBody(pass, skyMeshes, prepared, state, daylight, SkyPipelines.BODY, 1.0F);
            }
            SkyShaderCompat.setPhase(SkyShaderCompat.Phase.SKY);
            if (sky.darkDisc() && state.shouldRenderDarkDisc) {
                Matrix4f darkDisc = RenderSystem.getModelViewMatrixCopy().translate(0.0F, DARK_DISC_OFFSET, 0.0F);
                drawDisc(pass, skyMeshes.bottomDisc(), darkDisc, new Vector3f(0.0F, 0.0F, 0.0F));
            }
            SkyShaderCompat.setPhase(SkyShaderCompat.Phase.NONE);
        }
        return true;
    }

    public static void renderOverlay(float depthFar) {
        SkyRenderState state = overlayState;
        GpuBufferSlice fog = overlayFog;
        if (state == null) return;

        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;
        if (overlayLevel != level) return;
        overlayState = null;
        overlayFog = null;
        overlayLevel = null;
        DimensionSky sky = DimensionSkyRegistry.getForDimension(level.dimension().identifier());
        if (sky == null || sky.shaderSky() != ShaderSky.OVERLAY) return;

        SkyMeshes skyMeshes = meshesFor(sky);
        List<PreparedBody> bodies = prepareBodies(sky);
        float distance = depthFar * OVERLAY_DISTANCE;
        float scale = distance / BODY_DISTANCE;
        RenderTarget main = Minecraft.getInstance().gameRenderer.mainRenderTarget();

        if (fog != null) RenderSystem.setShaderFog(fog);
        try (RenderPass pass = RenderSystem.getDevice()
                .createCommandEncoder()
                .createRenderPass(() -> "GC custom sky overlay", main.getColorTextureView(), Optional.empty(),
                        main.getDepthTextureView(), OptionalDouble.empty())) {
            RenderSystem.bindDefaultUniforms(pass);
            if (sky.overlayFog().isPresent()) {
                drawFog(pass, skyMeshes, sky.overlayFog().get(), depthFar);
            }
            drawBackdrop(pass, skyMeshes, skyColor(sky, state), distance / CUBE_DIAGONAL);
            drawStars(pass, skyMeshes, sky, state.rainBrightness, SkyPipelines.OVERLAY_STARS, scale);
            float daylight = daylight(state.sunAngle);
            for (PreparedBody prepared : bodies) {
                drawBody(pass, skyMeshes, prepared, state, daylight, SkyPipelines.OVERLAY_BODY, scale);
            }
        }
    }

    private static void drawFog(RenderPass pass, SkyMeshes skyMeshes, OverlayFog fog, float depthFar) {
        float renderDistance = Minecraft.getInstance().options.getEffectiveRenderDistance() * 16.0F;
        float limit = depthFar * FOG_MAX_DISTANCE;
        float start = Math.min(fog.start() * renderDistance, limit);
        float end = Math.min(Math.max(fog.end() * renderDistance, start + 1.0F), limit);
        int color = fog.color();
        Vector3f rgb = new Vector3f(HexColor.red(color), HexColor.green(color), HexColor.blue(color));
        RenderSystem.AutoStorageIndexBuffer indices = RenderSystem.getSequentialBuffer(PrimitiveTopology.QUADS);

        pass.pushDebugGroup(() -> "GC sky overlay fog");
        pass.setPipeline(RenderSystem.getCompiledPipeline(SkyPipelines.OVERLAY_FOG));
        RenderSystem.bindDefaultUniforms(pass);
        pass.setVertexBuffer(0, skyMeshes.sphere().slice());
        pass.setIndexBuffer(indices.getBuffer(SkyMeshes.SPHERE_INDEX_COUNT), indices.type());
        float previous = 0.0F;
        for (int shell = 1; shell <= FOG_SHELLS; shell++) {
            float density = (float) shell / FOG_SHELLS;
            float alpha = previous >= 1.0F ? 1.0F : 1.0F - (1.0F - density) / (1.0F - previous);
            previous = density;
            float radius = Mth.lerp(density, start, end);
            Matrix4f modelView = RenderSystem.getModelViewMatrixCopy().scale(radius);
            pass.setUniform("DynamicTransforms",
                    RenderSystem.getDynamicUniforms().writeTransform(modelView, new Vector4f(rgb, alpha)));
            pass.drawIndexed(SkyMeshes.SPHERE_INDEX_COUNT, 1, 0, 0, 0);
        }
        pass.popDebugGroup();
    }

    private static void drawBackdrop(RenderPass pass, SkyMeshes skyMeshes, Vector3fc color, float halfSize) {
        Matrix4f modelView = RenderSystem.getModelViewMatrixCopy().scale(halfSize);
        GpuBufferSlice transforms = RenderSystem.getDynamicUniforms().writeTransform(modelView, new Vector4f(color, 1.0F));
        RenderSystem.AutoStorageIndexBuffer indices = RenderSystem.getSequentialBuffer(PrimitiveTopology.QUADS);
        pass.pushDebugGroup(() -> "GC sky overlay backdrop");
        pass.setPipeline(RenderSystem.getCompiledPipeline(SkyPipelines.OVERLAY_BACKDROP));
        RenderSystem.bindDefaultUniforms(pass);
        pass.setUniform("DynamicTransforms", transforms);
        pass.setVertexBuffer(0, skyMeshes.backdrop().slice());
        pass.setIndexBuffer(indices.getBuffer(SkyMeshes.BACKDROP_INDEX_COUNT), indices.type());
        pass.drawIndexed(SkyMeshes.BACKDROP_INDEX_COUNT, 1, 0, 0, 0);
        pass.popDebugGroup();
    }

    private static SkyMeshes meshesFor(DimensionSky sky) {
        StarDensity density = StarDensity.byName(GCConfig.starDensity());
        if (meshDensity != density) {
            closeMeshes();
            meshDensity = density;
        }
        SkyMeshes cached = MESHES.get(sky);
        if (cached == null) {
            var registered = DimensionSkyRegistry.getAll();
            MESHES.entrySet().removeIf(entry -> {
                boolean stale = registered.stream().noneMatch(known -> known == entry.getKey());
                if (stale) entry.getValue().close();
                return stale;
            });
            cached = SkyMeshes.build(sky.stars().orElse(null), density.multiplier());
            MESHES.put(sky, cached);
        }
        return cached;
    }

    private static void closeMeshes() {
        for (SkyMeshes built : MESHES.values()) built.close();
        MESHES.clear();
    }

    private static List<PreparedBody> prepareBodies(DimensionSky sky) {
        TextureManager textures = Minecraft.getInstance().getTextureManager();
        List<PreparedBody> prepared = new ArrayList<>(sky.bodies().size());
        for (SkyBody entry : sky.bodies()) {
            Optional<ResolvedSkyBody> resolved = entry.resolve();
            if (resolved.isEmpty()) {
                warnMissingBody(entry);
                continue;
            }
            ResolvedSkyBody body = resolved.get();
            AbstractTexture texture = body.blend() == SkyBody.Blend.ADDITIVE
                    ? SkyGlowTextures.get(body.texture())
                    : textures.getTexture(body.texture());
            prepared.add(new PreparedBody(body, texture));
        }
        return prepared;
    }

    private static void warnMissingBody(SkyBody entry) {
        entry.body().ifPresent(id -> {
            if (MISSING_BODIES.add(id)) {
                Constants.LOG.warn("Dimension sky references unknown celestial body {}", id);
            }
        });
    }

    private static Vector3fc skyColor(DimensionSky sky, SkyRenderState state) {
        if (sky.skyColor().isPresent()) {
            int color = sky.skyColor().get();
            return new Vector3f(HexColor.red(color), HexColor.green(color), HexColor.blue(color));
        }
        return state.skyColor != null ? state.skyColor : new Vector3f(0.0F, 0.0F, 0.0F);
    }

    private static void drawDisc(RenderPass pass, GpuBuffer disc, Matrix4f modelView, Vector3fc color) {
        GpuBufferSlice transforms = RenderSystem.getDynamicUniforms().writeTransform(modelView, new Vector4f(color, 1.0F));
        pass.pushDebugGroup(() -> "GC sky disc");
        pass.setPipeline(RenderSystem.getCompiledPipeline(RenderPipelines.SKY));
        pass.setUniform("DynamicTransforms", transforms);
        pass.setVertexBuffer(0, disc.slice());
        pass.draw(SkyMeshes.DISC_VERTICES, 1, 0, 0);
        pass.popDebugGroup();
    }

    private static void drawStars(RenderPass pass, SkyMeshes skyMeshes, DimensionSky sky, float rainBrightness,
                                  RenderPipeline pipeline, float scale) {
        GpuBuffer stars = skyMeshes.stars();
        if (stars == null || sky.stars().isEmpty()) return;

        SkyStars settings = sky.stars().get();
        float brightness = settings.brightness() * (settings.fadesInRain() ? rainBrightness : 1.0F);
        if (brightness <= 0.0F) return;

        RenderSystem.AutoStorageIndexBuffer indices = RenderSystem.getSequentialBuffer(PrimitiveTopology.QUADS);
        int color = settings.color();
        Matrix4f modelView = RenderSystem.getModelViewMatrixCopy().scale(scale);
        pass.pushDebugGroup(() -> "GC sky stars");
        pass.setPipeline(RenderSystem.getCompiledPipeline(pipeline));
        RenderSystem.bindDefaultUniforms(pass);
        pass.setVertexBuffer(0, stars.slice());
        pass.setIndexBuffer(indices.getBuffer(skyMeshes.starIndexCount()), indices.type());

        if (settings.twinkle() <= 0.0F) {
            pass.setUniform("DynamicTransforms", starTransform(modelView, color, brightness));
            pass.drawIndexed(skyMeshes.starIndexCount(), 1, 0, 0, 0);
        } else {
            float time = (Util.getMillis() % 3_600_000L) / 1000.0F * settings.twinkleSpeed();
            for (int group = 0; group < SkyMeshes.STAR_GROUPS; group++) {
                int quads = skyMeshes.groupQuadCount(group);
                if (quads == 0) continue;
                float shimmer = twinkle(time, group);
                float groupBrightness = brightness * (1.0F - settings.twinkle() * shimmer);
                pass.setUniform("DynamicTransforms", starTransform(modelView, color, groupBrightness));
                pass.drawIndexed(quads * 6, 1, 0, skyMeshes.groupFirstQuad(group) * 4, 0);
            }
        }
        pass.popDebugGroup();
    }

    private static GpuBufferSlice starTransform(Matrix4f modelView, int color, float brightness) {
        return RenderSystem.getDynamicUniforms().writeTransform(modelView,
                new Vector4f(HexColor.red(color), HexColor.green(color), HexColor.blue(color), brightness));
    }

    private static float twinkle(float time, int group) {
        float phase = group * GOLDEN_ANGLE;
        float rate = 0.55F + 0.9F * ((group * 0.618034F) % 1.0F);
        float wave = 0.6F * Mth.sin(time * rate * TWO_PI + phase)
                + 0.4F * Mth.sin(time * rate * 2.3F * TWO_PI + phase * 1.7F);
        return 0.5F + 0.5F * wave;
    }

    private static float daylight(float sunAngle) {
        return Mth.clamp((Mth.cos(sunAngle) + DAY_FADE) / (DAY_FADE * 2.0F), 0.0F, 1.0F);
    }

    private static float visibility(SkyBody.Visibility visibility, float daylight) {
        return switch (visibility) {
            case ALWAYS -> 1.0F;
            case DAY -> daylight;
            case NIGHT -> 1.0F - daylight;
        };
    }

    private static Matrix4f bodyTransform(ResolvedSkyBody body, SkyRenderState state, float scale) {
        Matrix4f modelView = RenderSystem.getModelViewMatrixCopy().scale(scale);
        if (body.orbit().isPresent()) {
            SkyOrbit orbit = body.orbit().get();
            float angle = orbit.follows() == SkyOrbit.Follows.MOON ? state.moonAngle : state.sunAngle;
            modelView.rotateY((float) Math.toRadians(-90.0F - body.yaw()))
                    .rotateZ((float) Math.toRadians(orbit.tilt()))
                    .rotateX(angle + (float) Math.toRadians(orbit.offset()));
        } else {
            modelView.rotateY((float) Math.toRadians(-body.yaw()))
                    .rotateX((float) Math.toRadians(body.pitch() - 90.0F));
        }
        return modelView.rotateY((float) Math.toRadians(body.roll()))
                .translate(0.0F, BODY_DISTANCE, 0.0F)
                .scale(body.size(), 1.0F, body.size());
    }

    private static void drawBody(RenderPass pass, SkyMeshes skyMeshes, PreparedBody prepared, SkyRenderState state,
                                 float daylight, RenderPipeline pipeline, float scale) {
        ResolvedSkyBody body = prepared.body();
        float alpha = HexColor.alpha(body.color())
                * (body.fadesInRain() ? state.rainBrightness : 1.0F)
                * visibility(body.visibility(), daylight);
        if (alpha <= 0.0F || body.size() <= 0.0F) return;

        Matrix4f modelView = bodyTransform(body, state, scale);

        Vector4f tint = new Vector4f(HexColor.red(body.color()), HexColor.green(body.color()), HexColor.blue(body.color()), alpha);
        GpuBufferSlice transforms = RenderSystem.getDynamicUniforms().writeTransform(modelView, tint);
        RenderSystem.AutoStorageIndexBuffer indices = RenderSystem.getSequentialBuffer(PrimitiveTopology.QUADS);

        pass.pushDebugGroup(() -> "GC sky body");
        pass.setPipeline(RenderSystem.getCompiledPipeline(pipeline));
        RenderSystem.bindDefaultUniforms(pass);
        pass.setUniform("DynamicTransforms", transforms);
        pass.setUniform("Sampler0", prepared.texture().getTextureView(), prepared.texture().getSampler());
        pass.setVertexBuffer(0, skyMeshes.quad().slice());
        pass.setIndexBuffer(indices.getBuffer(6), indices.type());
        pass.drawIndexed(6, 1, 0, 0, 0);
        pass.popDebugGroup();
    }
}

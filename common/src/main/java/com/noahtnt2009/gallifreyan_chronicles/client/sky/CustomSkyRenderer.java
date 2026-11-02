package com.noahtnt2009.gallifreyan_chronicles.client.sky;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.noahtnt2009.gallifreyan_chronicles.config.GCConfig;
import com.noahtnt2009.gallifreyan_chronicles.sky.DimensionSky;
import com.noahtnt2009.gallifreyan_chronicles.sky.DimensionSkyRegistry;
import com.noahtnt2009.gallifreyan_chronicles.sky.HexColor;
import com.noahtnt2009.gallifreyan_chronicles.sky.SkyBody;
import com.noahtnt2009.gallifreyan_chronicles.sky.SkyStars;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;

public final class CustomSkyRenderer {
    private static final float BODY_DISTANCE = 100.0F;
    private static final float DARK_DISC_OFFSET = 12.0F;

    private static @Nullable DimensionSky meshOwner;
    private static @Nullable StarDensity meshDensity;
    private static @Nullable SkyMeshes meshes;

    private record PreparedBody(SkyBody body, AbstractTexture texture) {
    }

    private CustomSkyRenderer() {
    }

    public static boolean render(RenderTarget target, GpuBufferSlice skyFog, SkyRenderState state) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return false;

        DimensionSky sky = DimensionSkyRegistry.getForDimension(level.dimension().identifier());
        if (sky == null) return false;

        SkyShaderCompat.assignPipelines();
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
            drawStars(pass, skyMeshes, sky, state.rainBrightness);
            SkyShaderCompat.setPhase(SkyShaderCompat.Phase.CUSTOM_SKY);
            for (PreparedBody prepared : bodies) {
                drawBody(pass, skyMeshes, prepared, state.rainBrightness);
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

    private static SkyMeshes meshesFor(DimensionSky sky) {
        StarDensity density = StarDensity.byName(GCConfig.starDensity());
        if (meshes == null || meshOwner != sky || meshDensity != density) {
            if (meshes != null) meshes.close();
            if (meshOwner != sky) SkyGlowTextures.clear();
            meshes = SkyMeshes.build(sky.stars().orElse(null), density.multiplier());
            meshOwner = sky;
            meshDensity = density;
        }
        return meshes;
    }

    private static List<PreparedBody> prepareBodies(DimensionSky sky) {
        TextureManager textures = Minecraft.getInstance().getTextureManager();
        List<PreparedBody> prepared = new ArrayList<>(sky.bodies().size());
        for (SkyBody body : sky.bodies()) {
            AbstractTexture texture = body.blend() == SkyBody.Blend.ADDITIVE
                    ? SkyGlowTextures.get(body.texture())
                    : textures.getTexture(body.texture());
            prepared.add(new PreparedBody(body, texture));
        }
        return prepared;
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

    private static void drawStars(RenderPass pass, SkyMeshes skyMeshes, DimensionSky sky, float rainBrightness) {
        GpuBuffer stars = skyMeshes.stars();
        if (stars == null || sky.stars().isEmpty()) return;

        SkyStars settings = sky.stars().get();
        float brightness = settings.brightness() * (settings.fadesInRain() ? rainBrightness : 1.0F);
        if (brightness <= 0.0F) return;

        RenderSystem.AutoStorageIndexBuffer indices = RenderSystem.getSequentialBuffer(PrimitiveTopology.QUADS);
        int color = settings.color();
        GpuBufferSlice transforms = RenderSystem.getDynamicUniforms().writeTransform(
                RenderSystem.getModelViewMatrixCopy(),
                new Vector4f(HexColor.red(color), HexColor.green(color), HexColor.blue(color), brightness));
        pass.pushDebugGroup(() -> "GC sky stars");
        pass.setPipeline(RenderSystem.getCompiledPipeline(RenderPipelines.STARS));
        RenderSystem.bindDefaultUniforms(pass);
        pass.setUniform("DynamicTransforms", transforms);
        pass.setVertexBuffer(0, stars.slice());
        pass.setIndexBuffer(indices.getBuffer(skyMeshes.starIndexCount()), indices.type());
        pass.drawIndexed(skyMeshes.starIndexCount(), 1, 0, 0, 0);
        pass.popDebugGroup();
    }

    private static void drawBody(RenderPass pass, SkyMeshes skyMeshes, PreparedBody prepared, float rainBrightness) {
        SkyBody body = prepared.body();
        float alpha = HexColor.alpha(body.color()) * (body.fadesInRain() ? rainBrightness : 1.0F);
        if (alpha <= 0.0F || body.size() <= 0.0F) return;

        Matrix4f modelView = RenderSystem.getModelViewMatrixCopy()
                .rotateY((float) Math.toRadians(-body.yaw()))
                .rotateX((float) Math.toRadians(body.pitch() - 90.0F))
                .rotateY((float) Math.toRadians(body.roll()))
                .translate(0.0F, BODY_DISTANCE, 0.0F)
                .scale(body.size(), 1.0F, body.size());

        Vector4f tint = new Vector4f(HexColor.red(body.color()), HexColor.green(body.color()), HexColor.blue(body.color()), alpha);
        GpuBufferSlice transforms = RenderSystem.getDynamicUniforms().writeTransform(modelView, tint);
        RenderSystem.AutoStorageIndexBuffer indices = RenderSystem.getSequentialBuffer(PrimitiveTopology.QUADS);

        pass.pushDebugGroup(() -> "GC sky body");
        pass.setPipeline(RenderSystem.getCompiledPipeline(SkyPipelines.BODY));
        RenderSystem.bindDefaultUniforms(pass);
        pass.setUniform("DynamicTransforms", transforms);
        pass.setUniform("Sampler0", prepared.texture().getTextureView(), prepared.texture().getSampler());
        pass.setVertexBuffer(0, skyMeshes.quad().slice());
        pass.setIndexBuffer(indices.getBuffer(6), indices.type());
        pass.drawIndexed(6, 1, 0, 0, 0);
        pass.popDebugGroup();
    }
}

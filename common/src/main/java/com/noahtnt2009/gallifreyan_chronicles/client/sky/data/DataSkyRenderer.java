package com.noahtnt2009.gallifreyan_chronicles.client.sky.data;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.noahtnt2009.gallifreyan_chronicles.client.sky.DimensionSkyRenderer;
import com.noahtnt2009.gallifreyan_chronicles.mixin.MinecraftAtlasManagerAccessor;
import com.noahtnt2009.gallifreyan_chronicles.mixin.SkyRendererInvoker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.AtlasManager;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.MoonPhase;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;

public class DataSkyRenderer implements DimensionSkyRenderer {
    private static final Identifier SUN_SPRITE = Identifier.withDefaultNamespace("sun");

    private static final float SUN_BASE_SCALE = 30.0F;
    private static final float MOON_BASE_SCALE = 20.0F;

    private static final List<GpuBuffer> PENDING_BUFFERS = new ArrayList<>();

    private final DimensionSky sky;

    public DataSkyRenderer(DimensionSky sky) {
        this.sky = sky;
    }

    @Override
    public void renderSunMoonAndStars(
            SkyRendererInvoker invoker,
            RenderPass renderPass,
            PoseStack poseStack,
            float sunAngle,
            float moonAngle,
            float starAngle,
            MoonPhase moonPhase,
            float rainBrightness,
            float starBrightness
    ) {
        PENDING_BUFFERS.forEach(GpuBuffer::close);
        PENDING_BUFFERS.clear();

        for (SkyCelestialBody body : sky.bodies()) {
            poseStack.pushPose();
            poseStack.rotateDegrees(Axis.YP, -89.0F);
            poseStack.pushPose();

            switch (body.type()) {
                case SUN -> {
                    poseStack.rotate(Axis.XP, sunAngle);
                    if (body.yaw() != 0.0f) poseStack.rotateDegrees(Axis.YP, body.yaw());
                    if (body.distance() != 0.0f) poseStack.translate(body.distance(), 0.0F, 0.0F);
                    renderCustomSun(renderPass, poseStack, body.scale(), rainBrightness);
                }
                case MOON -> {
                    poseStack.rotate(Axis.XP, moonAngle);
                    if (body.yaw() != 0.0f) poseStack.rotateDegrees(Axis.YP, body.yaw());
                    if (body.distance() != 0.0f) poseStack.translate(body.distance(), 0.0F, 0.0F);
                    MoonPhase phase = body.useMoonPhase() ? moonPhase : MoonPhase.FULL_MOON;
                    renderCustomMoon(renderPass, poseStack, phase, body.scale(), rainBrightness);
                }
            }

            poseStack.popPose();
            poseStack.popPose();
        }

        if (starBrightness > 0.0F) {
            poseStack.pushPose();
            poseStack.rotateDegrees(Axis.YP, -90.0F);
            poseStack.rotate(Axis.XP, starAngle);
            invoker.invokeRenderStars(renderPass, starBrightness, poseStack);
            poseStack.popPose();
        }
    }

    private void renderCustomSun(RenderPass renderPass, PoseStack poseStack, float scale, float rainBrightness) {
        TextureAtlas atlas = getCelestialsAtlas();
        TextureAtlasSprite sprite = atlas.getSprite(SUN_SPRITE);
        renderQuad(renderPass, poseStack, atlas, sprite, scale * SUN_BASE_SCALE, rainBrightness, false);
    }

    private void renderCustomMoon(RenderPass renderPass, PoseStack poseStack, MoonPhase phase, float scale, float rainBrightness) {
        TextureAtlas atlas = getCelestialsAtlas();
        TextureAtlasSprite sprite = atlas.getSprite(Identifier.withDefaultNamespace("moon/" + phase.getSerializedName()));
        renderQuad(renderPass, poseStack, atlas, sprite, scale * MOON_BASE_SCALE, rainBrightness, true);
    }

    @SuppressWarnings("resource")
    private TextureAtlas getCelestialsAtlas() {
        AtlasManager atlasManager = ((MinecraftAtlasManagerAccessor) Minecraft.getInstance())
                .gallifreyan_chronicles$getAtlasManager();
        return atlasManager.getAtlasOrThrow(AtlasIds.CELESTIALS);
    }

    private void renderQuad(
            RenderPass renderPass,
            PoseStack poseStack,
            TextureAtlas atlas,
            TextureAtlasSprite sprite,
            float effectiveScale,
            float rainBrightness,
            boolean flipUv
    ) {
        Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushMatrix();
        modelViewStack.mul(poseStack.last().pose());
        modelViewStack.translate(0.0F, 100.0F, 0.0F);
        modelViewStack.scale(effectiveScale, 1.0F, effectiveScale);
        Matrix4f modelViewMatrix = new Matrix4f(modelViewStack);
        modelViewStack.popMatrix();

        GpuBuffer buffer;
        try (ByteBufferBuilder byteBufferBuilder = ByteBufferBuilder.exactlySized(
                4 * DefaultVertexFormat.POSITION_TEX.getVertexSize())) {
            BufferBuilder bufferBuilder = new BufferBuilder(
                    byteBufferBuilder,
                    PrimitiveTopology.QUADS,
                    DefaultVertexFormat.POSITION_TEX
            );

            if (!flipUv) {
                bufferBuilder.addVertex(-1.0F, 0.0F, -1.0F).setUv(sprite.getU0(), sprite.getV0());
                bufferBuilder.addVertex(1.0F, 0.0F, -1.0F).setUv(sprite.getU1(), sprite.getV0());
                bufferBuilder.addVertex(1.0F, 0.0F, 1.0F).setUv(sprite.getU1(), sprite.getV1());
                bufferBuilder.addVertex(-1.0F, 0.0F, 1.0F).setUv(sprite.getU0(), sprite.getV1());
            } else {
                bufferBuilder.addVertex(-1.0F, 0.0F, -1.0F).setUv(sprite.getU1(), sprite.getV1());
                bufferBuilder.addVertex(1.0F, 0.0F, -1.0F).setUv(sprite.getU0(), sprite.getV1());
                bufferBuilder.addVertex(1.0F, 0.0F, 1.0F).setUv(sprite.getU0(), sprite.getV0());
                bufferBuilder.addVertex(-1.0F, 0.0F, 1.0F).setUv(sprite.getU1(), sprite.getV0());
            }

            try (MeshData mesh = bufferBuilder.buildOrThrow()) {
                buffer = RenderSystem.getDevice().createBuffer(() -> "GC celestial quad", 32, mesh.vertexBuffer());
            }
        }
        PENDING_BUFFERS.add(buffer);

        GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms().writeTransform(
                modelViewMatrix,
                new Vector4f(1.0F, 1.0F, 1.0F, rainBrightness)
        );

        RenderSystem.AutoStorageIndexBuffer quadIndices = RenderSystem.getSequentialBuffer(PrimitiveTopology.QUADS);
        GpuBuffer indexBuffer = quadIndices.getBuffer(6);

        renderPass.setPipeline(RenderSystem.getCompiledPipeline(RenderPipelines.CELESTIAL));
        RenderSystem.bindDefaultUniforms(renderPass);
        renderPass.setUniform("DynamicTransforms", dynamicTransforms);
        renderPass.setUniform("Sampler0", atlas.getTextureView(), atlas.getSampler());
        renderPass.setVertexBuffer(0, buffer.slice());
        renderPass.setIndexBuffer(indexBuffer, quadIndices.type());
        renderPass.drawIndexed(6, 1, 0, 0, 0);
    }

    @Override
    public boolean shouldRenderDarkDisc(float deltaPartialTick) {
        return sky.renderDarkDisc();
    }
}
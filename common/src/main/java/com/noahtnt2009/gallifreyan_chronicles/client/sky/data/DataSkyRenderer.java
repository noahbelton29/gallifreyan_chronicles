package com.noahtnt2009.gallifreyan_chronicles.client.sky.data;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.math.Axis;
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

import java.util.Optional;
import java.util.OptionalDouble;

public class DataSkyRenderer implements DimensionSkyRenderer {
    private static final Identifier SUN_SPRITE = Identifier.withDefaultNamespace("sun");

    private static final float SUN_BASE_SCALE = 30.0F;
    private static final float MOON_BASE_SCALE = 20.0F;

    private final DimensionSky sky;

    public DataSkyRenderer(DimensionSky sky) {
        this.sky = sky;
    }

    @Override
    public void renderSunMoonAndStars(
            SkyRendererInvoker invoker,
            PoseStack poseStack,
            float sunAngle,
            float moonAngle,
            float starAngle,
            MoonPhase moonPhase,
            float rainBrightness,
            float starBrightness
    ) {
        for (SkyCelestialBody body : sky.bodies()) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(-89.0F));
            poseStack.pushPose();

            switch (body.type()) {
                case SUN -> {
                    poseStack.mulPose(Axis.XP.rotation(sunAngle));
                    if (body.yaw() != 0.0f) poseStack.mulPose(Axis.YP.rotationDegrees(body.yaw()));
                    if (body.distance() != 0.0f) poseStack.translate(body.distance(), 0.0F, 0.0F);
                    renderCustomSun(poseStack, body.scale(), rainBrightness);
                }
                case MOON -> {
                    poseStack.mulPose(Axis.XP.rotation(moonAngle));
                    if (body.yaw() != 0.0f) poseStack.mulPose(Axis.YP.rotationDegrees(body.yaw()));
                    if (body.distance() != 0.0f) poseStack.translate(body.distance(), 0.0F, 0.0F);
                    MoonPhase phase = body.useMoonPhase() ? moonPhase : MoonPhase.FULL_MOON;
                    renderCustomMoon(poseStack, phase, body.scale(), rainBrightness);
                }
            }

            poseStack.popPose();
            poseStack.popPose();
        }

        if (starBrightness > 0.0F) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
            poseStack.mulPose(Axis.XP.rotation(starAngle));
            invoker.invokeRenderStars(starBrightness, poseStack);
            poseStack.popPose();
        }
    }

    private void renderCustomSun(PoseStack poseStack, float scale, float rainBrightness) {
        TextureAtlas atlas = getCelestialsAtlas();
        TextureAtlasSprite sprite = atlas.getSprite(SUN_SPRITE);
        renderQuad(poseStack, atlas, sprite, scale * SUN_BASE_SCALE, rainBrightness, false);
    }

    private void renderCustomMoon(PoseStack poseStack, MoonPhase phase, float scale, float rainBrightness) {
        TextureAtlas atlas = getCelestialsAtlas();
        TextureAtlasSprite sprite = atlas.getSprite(Identifier.withDefaultNamespace("moon/" + phase.getSerializedName()));
        renderQuad(poseStack, atlas, sprite, scale * MOON_BASE_SCALE, rainBrightness, true);
    }

    private TextureAtlas getCelestialsAtlas() {
        AtlasManager atlasManager = ((MinecraftAtlasManagerAccessor) Minecraft.getInstance())
                .gallifreyan_chronicles$getAtlasManager();
        return atlasManager.getAtlasOrThrow(AtlasIds.CELESTIALS);
    }

    private void renderQuad(
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

            try (MeshData mesh = bufferBuilder.buildOrThrow();
                 GpuBuffer buffer = RenderSystem.getDevice().createBuffer(() -> "GC celestial quad", 32, mesh.vertexBuffer())) {

                RenderTarget target = Minecraft.getInstance().gameRenderer.mainRenderTarget();
                GpuTextureView colorView = target.getColorTextureView();
                GpuTextureView depthView = target.getDepthTextureView();

                GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms().writeTransform(
                        new Matrix4f(modelViewStack),
                        new Vector4f(1.0F, 1.0F, 1.0F, rainBrightness)
                );

                RenderSystem.AutoStorageIndexBuffer quadIndices = RenderSystem.getSequentialBuffer(PrimitiveTopology.QUADS);
                GpuBuffer indexBuffer = quadIndices.getBuffer(6);

                assert colorView != null;
                try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                        () -> "GC celestial", colorView, Optional.empty(), depthView, OptionalDouble.empty())) {
                    renderPass.setPipeline(RenderPipelines.CELESTIAL);
                    RenderSystem.bindDefaultUniforms(renderPass);
                    renderPass.setUniform("DynamicTransforms", dynamicTransforms);
                    renderPass.bindTexture("Sampler0", atlas.getTextureView(), atlas.getSampler());
                    renderPass.setVertexBuffer(0, buffer.slice());
                    renderPass.setIndexBuffer(indexBuffer, quadIndices.type());
                    renderPass.drawIndexed(6, 1, 0, 0, 0);
                }
            }
        }

        modelViewStack.popMatrix();
    }

    @Override
    public boolean shouldRenderDarkDisc(float deltaPartialTick) {
        return sky.renderDarkDisc();
    }
}
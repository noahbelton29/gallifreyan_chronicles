package com.noahtnt2009.gallifreyan_chronicles.client.gui.hud;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.device.GpuDevice;
import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.noahtnt2009.gallifreyan_chronicles.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.OptionalDouble;

public final class PowerDarknessTint {
    private static @Nullable RenderPipeline pipeline;
    private static @Nullable GpuTexture texture;
    private static @Nullable GpuTextureView textureView;
    private static int uploadedAlpha = -1;
    private static boolean failed;

    private PowerDarknessTint() {
    }

    private static @Nullable RenderPipeline pipeline() {
        if (pipeline == null && !failed) {
            try {
                pipeline = RenderPipelines.register(RenderPipeline.builder()
                        .withLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "pipeline/power_darkness_tint"))
                        .withBindGroupLayout(BindGroupLayouts.GLOBALS)
                        .withVertexShader("core/screenquad")
                        .withFragmentShader("core/blit_screen")
                        .withBindGroupLayout(BindGroupLayouts.IN_SAMPLER)
                        .withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
                        .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                        .withDepthStencilState(Optional.empty())
                        .withCull(false)
                        .build());
            } catch (RuntimeException e) {
                failed = true;
                Constants.LOG.error("failed to create power darkness tint pipeline", e);
            }
        }
        return pipeline;
    }

    private static GpuTextureView textureFor(int alpha) {
        GpuDevice device = RenderSystem.getDevice();
        if (texture == null) {
            texture = device.createTexture(Constants.MOD_ID + " power darkness tint",
                    GpuTexture.USAGE_TEXTURE_BINDING | GpuTexture.USAGE_COPY_DST,
                    GpuFormat.RGBA8_UNORM, 1, 1, 1, 1);
            textureView = device.createTextureView(texture);
            uploadedAlpha = -1;
        }
        if (uploadedAlpha != alpha) {
            ByteBuffer pixel = ByteBuffer.allocateDirect(4);
            pixel.put((byte) 0).put((byte) 0).put((byte) 0).put((byte) alpha);
            pixel.flip();
            device.createCommandEncoder().writeToTexture(texture, pixel, 0, 0, 0, 0, 1, 1);
            uploadedAlpha = alpha;
        }
        return textureView;
    }

    public static void draw(float opacity) {
        int alpha = Mth.clamp(Math.round(opacity * 255.0F), 0, 255);
        if (alpha <= 0) return;
        RenderPipeline tintPipeline = pipeline();
        if (tintPipeline == null) return;
        RenderTarget target = Minecraft.getInstance().gameRenderer.mainRenderTarget();
        if (target.getColorTextureView() == null || target.getDepthTextureView() == null) return;
        GpuTextureView view = textureFor(alpha);

        try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                () -> Constants.MOD_ID + " power darkness tint",
                target.getColorTextureView(),
                Optional.empty(),
                target.getDepthTextureView(),
                OptionalDouble.empty(),
                new RenderPass.RenderArea(0, 0, target.width, target.height))) {
            pass.setPipeline(RenderSystem.getCompiledPipeline(tintPipeline));
            RenderSystem.bindDefaultUniforms(pass);
            pass.setUniform("InSampler", view, RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST));
            pass.draw(3, 1, 0, 0);
        }
    }
}

package com.noahtnt2009.gallifreyan_chronicles.mixin.client.sky;

import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.noahtnt2009.gallifreyan_chronicles.client.sky.CustomSkyRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LevelRenderer.class, priority = 1500)
public abstract class LevelRendererSkyOverlayMixin {
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lorg/joml/Matrix4fStack;popMatrix()Lorg/joml/Matrix4fStack;"))
    private void gc$renderSkyOverlay(GraphicsResourceAllocator resourceAllocator, boolean renderOutline,
                                     CameraRenderState cameraState, GpuBufferSlice terrainFog, Vector4f fogColor,
                                     boolean shouldRenderSky, boolean consistentDepthRequired, CallbackInfo ci) {
        CustomSkyRenderer.renderOverlay(cameraState.depthFar);
    }
}

package uk.nbforge.gallifreyan_chronicles.mixin.client.sky;

import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import uk.nbforge.gallifreyan_chronicles.client.sky.CustomSkyRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LevelRenderer.class, priority = 1500)
public abstract class LevelRendererSkyOverlayMixin {
    /* the sky canvas itself is drawn and laid on from LevelRendererFadeBoxMixin, so it goes under the fading box */
    @Inject(method = "render", at = @At("HEAD"))
    private void gc$beginSkyFrame(GraphicsResourceAllocator resourceAllocator, boolean renderOutline,
                                     CameraRenderState cameraState, GpuBufferSlice terrainFog, Vector4f fogColor,
                                     boolean shouldRenderSky, boolean consistentDepthRequired, CallbackInfo ci) {
        CustomSkyRenderer.beginFrame(cameraState.depthFar);
    }
}

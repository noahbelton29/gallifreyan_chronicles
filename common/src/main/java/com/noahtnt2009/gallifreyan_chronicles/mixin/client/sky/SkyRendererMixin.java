package com.noahtnt2009.gallifreyan_chronicles.mixin.client.sky;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.noahtnt2009.gallifreyan_chronicles.client.sky.CustomSkyRenderer;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SkyRenderer.class)
public abstract class SkyRendererMixin {
    @Shadow
    @Final
    private RenderTarget renderTarget;

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void gc$renderCustomSky(GpuBufferSlice skyFog, SkyRenderState state, CallbackInfo ci) {
        if (CustomSkyRenderer.render(renderTarget, skyFog, state)) {
            ci.cancel();
        }
    }
}

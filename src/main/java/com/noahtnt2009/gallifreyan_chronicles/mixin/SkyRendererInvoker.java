package com.noahtnt2009.gallifreyan_chronicles.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.world.level.MoonPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(SkyRenderer.class)
public interface SkyRendererInvoker {
    @Invoker(value = "renderSun", remap = false)
    void invokeRenderSun(float rainBrightness, PoseStack poseStack);

    @Invoker(value = "renderMoon", remap = false)
    void invokeRenderMoon(MoonPhase moonPhase, float rainBrightness, PoseStack poseStack);
}
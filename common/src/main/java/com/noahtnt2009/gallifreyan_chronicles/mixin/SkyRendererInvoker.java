package com.noahtnt2009.gallifreyan_chronicles.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.renderpearl.api.commands.RenderPass;
import net.minecraft.client.renderer.SkyRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(SkyRenderer.class)
public interface SkyRendererInvoker {
    @Invoker(value = "renderStars", remap = false)
    void invokeRenderStars(RenderPass renderPass, float starBrightness, PoseStack poseStack);
}
package com.noahtnt2009.gallifreyan_chronicles.client.sky;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.noahtnt2009.gallifreyan_chronicles.mixin.SkyRendererInvoker;
import net.minecraft.world.level.MoonPhase;

public interface DimensionSkyRenderer {
    void renderSunMoonAndStars(
            SkyRendererInvoker invoker,
            RenderPass renderPass,
            PoseStack poseStack,
            float sunAngle,
            float moonAngle,
            float starAngle,
            MoonPhase moonPhase,
            float rainBrightness,
            float starBrightness
    );

    boolean shouldRenderDarkDisc(float deltaPartialTick);
}
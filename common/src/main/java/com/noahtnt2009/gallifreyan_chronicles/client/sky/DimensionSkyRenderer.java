package com.noahtnt2009.gallifreyan_chronicles.client.sky;

import com.mojang.blaze3d.vertex.PoseStack;
import com.noahtnt2009.gallifreyan_chronicles.mixin.SkyRendererInvoker;
import net.minecraft.world.level.MoonPhase;

public interface DimensionSkyRenderer {
    void renderSunMoonAndStars(
            SkyRendererInvoker invoker,
            PoseStack poseStack,
            float sunAngle,
            float moonAngle,
            float starAngle,
            MoonPhase moonPhase,
            float rainBrightness,
            float starBrightness
    );

    default boolean shouldRenderDarkDisc(float deltaPartialTick) {
        return true;
    }
}
package com.noahtnt2009.gallifreyan_chronicles.client.sky;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.noahtnt2009.gallifreyan_chronicles.mixin.SkyRendererInvoker;
import net.minecraft.world.level.MoonPhase;

public class MoonSkyRenderer implements DimensionSkyRenderer {
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
        // todo render earth
    }

    @Override
    public boolean shouldRenderDarkDisc(float deltaPartialTick) {
        return false;
    }
}
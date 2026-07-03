package com.noahtnt2009.gallifreyan_chronicles.client.sky;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.noahtnt2009.gallifreyan_chronicles.mixin.SkyRendererInvoker;
import net.minecraft.world.level.MoonPhase;

public class GallifreySkyRenderer implements DimensionSkyRenderer {
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
        // Second sun
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotation(sunAngle));
        poseStack.mulPose(Axis.YP.rotationDegrees(25.0F));
        poseStack.translate(45.0F, 0.0F, 0.0F);
        poseStack.scale(0.6F, 0.6F, 0.6F);
        invoker.invokeRenderSun(rainBrightness, poseStack);
        poseStack.popPose();
        poseStack.popPose();

        // Moon 1
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotation(moonAngle));
        poseStack.scale(1.3F, 1.3F, 1.3F);
        invoker.invokeRenderMoon(moonPhase, rainBrightness, poseStack);
        poseStack.popPose();
        poseStack.popPose();

        // Moon 2
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotation(moonAngle));
        poseStack.mulPose(Axis.YP.rotationDegrees(40.0F));
        poseStack.translate(70.0F, 0.0F, 0.0F);
        poseStack.scale(0.7F, 0.7F, 0.7F);
        invoker.invokeRenderMoon(moonPhase, rainBrightness, poseStack);
        poseStack.popPose();
        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderDarkDisc(float deltaPartialTick) {
        return false;
    }
}
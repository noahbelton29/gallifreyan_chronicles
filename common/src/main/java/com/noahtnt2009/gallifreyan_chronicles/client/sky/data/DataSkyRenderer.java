package com.noahtnt2009.gallifreyan_chronicles.client.sky.data;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.noahtnt2009.gallifreyan_chronicles.client.sky.DimensionSkyRenderer;
import com.noahtnt2009.gallifreyan_chronicles.mixin.SkyRendererInvoker;
import net.minecraft.world.level.MoonPhase;

public class DataSkyRenderer implements DimensionSkyRenderer {
    private final DimensionSky sky;

    public DataSkyRenderer(DimensionSky sky) {
        this.sky = sky;
    }

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
        for (SkyCelestialBody body : sky.bodies()) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
            poseStack.pushPose();

            switch (body.type()) {
                case SUN -> {
                    poseStack.mulPose(Axis.XP.rotation(sunAngle));
                    if (body.yaw() != 0.0f) poseStack.mulPose(Axis.YP.rotationDegrees(body.yaw()));
                    if (body.distance() != 0.0f) poseStack.translate(body.distance(), 0.0F, 0.0F);
                    poseStack.scale(body.scale(), body.scale(), body.scale());
                    invoker.invokeRenderSun(rainBrightness, poseStack);
                }
                case MOON -> {
                    poseStack.mulPose(Axis.XP.rotation(moonAngle));
                    if (body.yaw() != 0.0f) poseStack.mulPose(Axis.YP.rotationDegrees(body.yaw()));
                    if (body.distance() != 0.0f) poseStack.translate(body.distance(), 0.0F, 0.0F);
                    poseStack.scale(body.scale(), body.scale(), body.scale());
                    MoonPhase phase = body.useMoonPhase() ? moonPhase : MoonPhase.FULL_MOON;
                    invoker.invokeRenderMoon(phase, rainBrightness, poseStack);
                }
            }

            poseStack.popPose();
            poseStack.popPose();
        }
    }

    @Override
    public boolean shouldRenderDarkDisc(float deltaPartialTick) {
        return sky.renderDarkDisc();
    }
}

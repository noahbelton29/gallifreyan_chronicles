package com.noahtnt2009.gallifreyan_chronicles.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.noahtnt2009.gallifreyan_chronicles.mixin.SkyRendererInvoker;
import com.noahtnt2009.gallifreyan_chronicles.registry.GCDimensions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.MoonPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SkyRenderer.class)
public class SkyRendererMixin {

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void gallifrey_overrideSkyColor(ClientLevel level, float partialTicks, Camera camera, SkyRenderState state, CallbackInfo ci) {
        if (level.dimensionTypeRegistration().is(GCDimensions.GALLIFREY_TYPE_KEY)) {
            state.skyColor = 0xFF8833;
        }
    }

    @Inject(method = "renderSunMoonAndStars", at = @At("HEAD"))
    private void renderGallifreySky(
            PoseStack poseStack,
            float sunAngle,
            float moonAngle,
            float starAngle,
            MoonPhase moonPhase,
            float rainBrightness,
            float starBrightness,
            CallbackInfo ci
    ) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;
        if (!level.dimension().equals(GCDimensions.GALLIFREY_LEVEL_KEY)) return;

        SkyRendererInvoker invoker = (SkyRendererInvoker) (Object) this;

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
}
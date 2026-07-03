package com.noahtnt2009.gallifreyan_chronicles.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.noahtnt2009.gallifreyan_chronicles.client.sky.DimensionSkyRenderer;
import com.noahtnt2009.gallifreyan_chronicles.init.client.GCSkyRenderers;
import com.noahtnt2009.gallifreyan_chronicles.mixin.SkyRendererInvoker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.world.level.MoonPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(SkyRenderer.class)
public class SkyRendererMixin {
    @Inject(method = "shouldRenderDarkDisc", at = @At("HEAD"), cancellable = true)
    private void gcDarkDisc(float deltaPartialTick, ClientLevel level, CallbackInfoReturnable<Boolean> cir) {
        GCSkyRenderers.get(level.dimension()).ifPresent(renderer ->
                cir.setReturnValue(renderer.shouldRenderDarkDisc(deltaPartialTick))
        );
    }

    @Inject(method = "renderSunMoonAndStars", at = @At("HEAD"))
    private void gcRenderSky(
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

        Optional<DimensionSkyRenderer> renderer = GCSkyRenderers.get(level.dimension());
        if (renderer.isEmpty()) return;

        renderer.get().renderSunMoonAndStars(
                (SkyRendererInvoker) (Object) this,
                poseStack, sunAngle, moonAngle, starAngle,
                moonPhase, rainBrightness, starBrightness
        );
    }
}
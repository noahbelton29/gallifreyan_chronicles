package com.noahtnt2009.gallifreyan_chronicles.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.noahtnt2009.gallifreyan_chronicles.client.sky.DimensionSkyRenderer;
import com.noahtnt2009.gallifreyan_chronicles.client.sky.data.DataSkyRenderer;
import com.noahtnt2009.gallifreyan_chronicles.client.sky.data.DimensionSky;
import com.noahtnt2009.gallifreyan_chronicles.client.sky.data.DimensionSkyRegistry;
import com.noahtnt2009.gallifreyan_chronicles.mixin.SkyRendererInvoker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.world.level.MoonPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(SkyRenderer.class)
public class SkyRendererMixin {
    @Inject(method = "shouldRenderDarkDisc", at = @At("HEAD"), cancellable = true)
    private void gcDarkDisc(float deltaPartialTick, ClientLevel level, CallbackInfoReturnable<Boolean> cir) {
        gallifreyan_Chronicles$resolveRenderer(level).ifPresent(renderer ->
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

        Optional<DimensionSkyRenderer> renderer = gallifreyan_Chronicles$resolveRenderer(level);
        if (renderer.isEmpty()) return;

        renderer.get().renderSunMoonAndStars(
                (SkyRendererInvoker) this,
                poseStack, sunAngle, moonAngle, starAngle,
                moonPhase, rainBrightness, starBrightness
        );
    }

    @Unique
    private static Optional<DimensionSkyRenderer> gallifreyan_Chronicles$resolveRenderer(ClientLevel level) {
        DimensionSky sky = DimensionSkyRegistry.getForDimension(level.dimension().identifier());
        if (sky == null) return Optional.empty();

        return Optional.of(new DataSkyRenderer(sky));
    }
}
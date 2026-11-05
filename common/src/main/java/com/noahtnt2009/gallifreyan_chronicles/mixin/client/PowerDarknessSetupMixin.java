package com.noahtnt2009.gallifreyan_chronicles.mixin.client;

import com.noahtnt2009.gallifreyan_chronicles.client.hud.GCPowerDarkness;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.BlindnessFogEnvironment;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlindnessFogEnvironment.class)
public abstract class PowerDarknessSetupMixin {
    @Unique
    private static final float MIN_FOG_END = 14.0F;

    @Inject(method = "setupFog", at = @At("HEAD"), cancellable = true)
    private void gcPowerDarkSetup(FogData fog, Camera camera, ClientLevel level, float renderDistance,
                                  DeltaTracker deltaTracker, CallbackInfo ci) {
        float darkness = GCPowerDarkness.darknessOf(level);
        if (darkness <= 0.0F) return;
        if (camera.entity() instanceof LivingEntity living && living.hasEffect(MobEffects.BLINDNESS)) return;

        float far = Minecraft.getInstance().options.getEffectiveRenderDistance() * 16.0F;
        float end = Mth.lerp(darkness, far, MIN_FOG_END);
        fog.environmentalStart = end * 0.25F;
        fog.environmentalEnd = end;
        fog.skyEnd = end * 0.8F;
        fog.cloudEnd = end * 0.8F;
        ci.cancel();
    }
}

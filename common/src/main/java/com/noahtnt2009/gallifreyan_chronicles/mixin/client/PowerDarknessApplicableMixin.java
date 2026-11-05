package com.noahtnt2009.gallifreyan_chronicles.mixin.client;

import com.noahtnt2009.gallifreyan_chronicles.client.gui.hud.GCPowerDarkness;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.BlindnessFogEnvironment;
import net.minecraft.client.renderer.fog.environment.MobEffectFogEnvironment;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.FogType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobEffectFogEnvironment.class)
public abstract class PowerDarknessApplicableMixin {
    @Inject(method = "isApplicable", at = @At("HEAD"), cancellable = true)
    private void gcPowerDarkApplicable(FogType fogType, Entity entity, CallbackInfoReturnable<Boolean> cir) {
        MobEffectFogEnvironment self = (MobEffectFogEnvironment) (Object) this;
        if (self.getMobEffect() == MobEffects.BLINDNESS && GCPowerDarkness.renderLevelDarkness() > 0.0F) {
            cir.setReturnValue(true);
        }
    }
}

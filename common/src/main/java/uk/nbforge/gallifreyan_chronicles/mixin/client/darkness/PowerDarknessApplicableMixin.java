package uk.nbforge.gallifreyan_chronicles.mixin.client.darkness;

import net.minecraft.client.renderer.fog.environment.MobEffectFogEnvironment;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.material.FogType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import uk.nbforge.gallifreyan_chronicles.client.gui.hud.PowerDarkness;

@Mixin(MobEffectFogEnvironment.class)
public abstract class PowerDarknessApplicableMixin {
    @Inject(method = "isApplicable", at = @At("HEAD"), cancellable = true)
    private void gcPowerDarkApplicable(FogType fogType, Entity entity, CallbackInfoReturnable<Boolean> cir) {
        MobEffectFogEnvironment self = (MobEffectFogEnvironment) (Object) this;
        if (self.getMobEffect() == MobEffects.BLINDNESS && PowerDarkness.renderLevelDarkness() > 0.0f) {
            cir.setReturnValue(true);
        }
    }
}

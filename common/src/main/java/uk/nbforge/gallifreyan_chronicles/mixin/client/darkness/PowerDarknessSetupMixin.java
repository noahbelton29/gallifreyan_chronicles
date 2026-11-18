package uk.nbforge.gallifreyan_chronicles.mixin.client.darkness;

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
import uk.nbforge.gallifreyan_chronicles.client.gui.hud.DangerAlarm;
import uk.nbforge.gallifreyan_chronicles.client.gui.hud.PowerDarkness;
import uk.nbforge.gallifreyan_chronicles.config.GCConfig;

@Mixin(BlindnessFogEnvironment.class)
public abstract class PowerDarknessSetupMixin {
    @Unique
    private static final float MIN_FOG_END = 14.0f;

    @Unique
    private static final float ALARM_CURVE = 0.3f;

    @Inject(method = "setupFog", at = @At("HEAD"), cancellable = true)
    private void gcPowerDarkSetup(
            FogData fog,
            Camera camera,
            ClientLevel level,
            float renderDistance,
            DeltaTracker deltaTracker,
            CallbackInfo ci) {
        float darkness = PowerDarkness.darknessOf(level);
        if (darkness <= 0.0f) return;
        float alarm = DangerAlarm.amountFor(level) * (float) GCConfig.powerDarknessStrength();
        if (alarm > 0.0f && alarm >= darkness) darkness = (float) Math.pow(alarm, ALARM_CURVE);
        if (camera.entity() instanceof LivingEntity living && living.hasEffect(MobEffects.BLINDNESS)) return;

        float far = Minecraft.getInstance().options.getEffectiveRenderDistance() * 16.0f;
        float end = Mth.lerp(darkness, far, MIN_FOG_END);
        fog.environmentalStart = end * 0.25f;
        fog.environmentalEnd = end;
        fog.skyEnd = end * 0.8f;
        fog.cloudEnd = end * 0.8f;
        ci.cancel();
    }
}

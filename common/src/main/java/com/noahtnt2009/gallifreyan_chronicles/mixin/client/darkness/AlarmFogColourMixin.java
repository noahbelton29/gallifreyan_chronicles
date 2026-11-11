package com.noahtnt2009.gallifreyan_chronicles.mixin.client.darkness;

import com.noahtnt2009.gallifreyan_chronicles.client.gui.hud.GCDangerAlarm;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogRenderer;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FogRenderer.class)
public abstract class AlarmFogColourMixin {
    @Unique
    private static final float RED = 1.0F, GREEN = 0.04F, BLUE = 0.04F;

    @Inject(method = "computeFogColor", at = @At("TAIL"))
    private void gcAlarmFogColour(Camera camera, float partialTicks, ClientLevel level, int renderDistance,
                                  float darkenWorldAmount, Vector4f dest, CallbackInfo ci) {
        float alarm = GCDangerAlarm.amountFor(level);
        if (alarm <= 0.0F) return;
        alarm = (float) Math.pow(alarm, 0.3F);
        dest.x += (RED - dest.x) * alarm;
        dest.y += (GREEN - dest.y) * alarm;
        dest.z += (BLUE - dest.z) * alarm;
    }
}

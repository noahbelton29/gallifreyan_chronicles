package uk.nbforge.gallifreyan_chronicles.mixin.client.darkness;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogRenderer;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import uk.nbforge.gallifreyan_chronicles.client.gui.hud.DangerAlarm;

@Mixin(FogRenderer.class)
public abstract class AlarmFogColourMixin {
    @Unique
    private static final float RED = 1.0f, GREEN = 0.04f, BLUE = 0.04f;

    @Inject(method = "computeFogColor", at = @At("TAIL"))
    private void gcAlarmFogColour(
            Camera camera,
            float partialTicks,
            ClientLevel level,
            int renderDistance,
            float darkenWorldAmount,
            Vector4f dest,
            CallbackInfo ci) {
        float alarm = DangerAlarm.amountFor(level);
        if (alarm <= 0.0f) return;
        alarm = (float) Math.pow(alarm, 0.3f);
        dest.x += (RED - dest.x) * alarm;
        dest.y += (GREEN - dest.y) * alarm;
        dest.z += (BLUE - dest.z) * alarm;
    }
}

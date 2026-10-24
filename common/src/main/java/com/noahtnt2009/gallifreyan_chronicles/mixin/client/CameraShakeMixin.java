package com.noahtnt2009.gallifreyan_chronicles.mixin.client;

import com.noahtnt2009.gallifreyan_chronicles.client.camera.GCCameraShake;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraShakeMixin {
    @Shadow
    protected abstract void setRotation(float yRot, float xRot);

    @Inject(method = "update", at = @At("TAIL"))
    private void gallifreyan_chronicles$applyDrumShake(DeltaTracker deltaTracker, CallbackInfo ci) {
        GCCameraShake.Sample shake = GCCameraShake.current();
        if (shake == null) return;

        Camera camera = (Camera) (Object) this;
        setRotation(camera.yRot() + shake.yaw(), camera.xRot() + shake.pitch());
        camera.rotation().rotateZ((float) Math.toRadians(shake.roll()));
    }
}

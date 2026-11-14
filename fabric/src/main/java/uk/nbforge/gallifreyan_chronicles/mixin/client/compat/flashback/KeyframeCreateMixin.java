package uk.nbforge.gallifreyan_chronicles.mixin.client.compat.flashback;

import uk.nbforge.gallifreyan_chronicles.compat.flashback.FlashbackChunks;
import net.minecraft.world.entity.Entity;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/* a camera keyframe made while the camera is inside the tardis is stored in the
   overworld's coordinates. see ReplayKeyframePortal */
@Pseudo
@Mixin(targets = "com.moulberry.flashback.keyframe.impl.CameraKeyframe", remap = false)
public abstract class KeyframeCreateMixin {
    @Shadow(remap = false) @Final public Vector3d position;
    @Shadow(remap = false) public float yaw;
    @Shadow(remap = false) public float pitch;

    @Inject(method = "<init>(Lnet/minecraft/world/entity/Entity;)V", at = @At("RETURN"), remap = false, require = 0)
    private void gc$overworldCoords(Entity entity, CallbackInfo ci) {
        float[] yawPitch = {yaw, pitch};
        FlashbackChunks.onKeyframeCreated(position, yawPitch);
        yaw = yawPitch[0];
        pitch = yawPitch[1];
    }
}

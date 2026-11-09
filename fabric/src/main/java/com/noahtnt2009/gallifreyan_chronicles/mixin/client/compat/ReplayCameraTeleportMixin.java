package com.noahtnt2009.gallifreyan_chronicles.mixin.client.compat;

import com.noahtnt2009.gallifreyan_chronicles.compat.flashback.FlashbackChunks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import qouteall.imm_ptl.core.teleportation.TeleportationUtil;

/* replay camera and doors. remembers which door the free camera went through, and
   while keyframes move the camera they handle doors themselves (ReplayKeyframePortal),
   so immersive portals doesn't also teleport it */
@Pseudo
@Mixin(targets = "qouteall.imm_ptl.core.teleportation.ClientTeleportationManager", remap = false)
public abstract class ReplayCameraTeleportMixin {
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Inject(method = "tryTeleport", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private static void gc$keyframesDecide(float partialTick, CallbackInfoReturnable cir) {
        if (FlashbackChunks.keyframesMovingCamera()) cir.setReturnValue(null);
    }

    @Inject(method = "teleportPlayer", at = @At("HEAD"), remap = false, require = 0)
    private static void gc$rememberDoor(TeleportationUtil.Teleportation teleportation, float partialTick, CallbackInfo ci) {
        FlashbackChunks.onCameraTeleport(teleportation.portal());
    }
}

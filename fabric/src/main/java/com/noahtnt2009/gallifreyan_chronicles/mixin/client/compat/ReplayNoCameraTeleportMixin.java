package com.noahtnt2009.gallifreyan_chronicles.mixin.client.compat;

import com.noahtnt2009.gallifreyan_chronicles.compat.flashback.FlashbackChunks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/* in a replay the camera must not go through portals. the replay can't
   follow it into the tardis, pulls it back, and the world stops drawing.
   we only stop the teleport itself ("no portal to go through"), the rest of
   immersive portals' per frame work still runs */
@Pseudo
@Mixin(targets = "qouteall.imm_ptl.core.teleportation.ClientTeleportationManager", remap = false)
public abstract class ReplayNoCameraTeleportMixin {
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Inject(method = "tryTeleport", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private static void gc$noTeleportInReplay(float partialTick, CallbackInfoReturnable cir) {
        if (FlashbackChunks.inReplay()) cir.setReturnValue(null);
    }
}

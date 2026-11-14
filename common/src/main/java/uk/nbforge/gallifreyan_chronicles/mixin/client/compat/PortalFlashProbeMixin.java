package uk.nbforge.gallifreyan_chronicles.mixin.client.compat;

import uk.nbforge.gallifreyan_chronicles.compat.portal.PortalDiagnostics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/* immersive portals has a "teleport flash" debug probe. every frame, all session, it builds a
   row of text (a bunch of String.format calls plus fog and sky attribute lookups) for a 96 frame
   ring. when you cross a portal it keeps recording for 40 more frames and then writes the whole
   ring, about 32 KB, to the log in one go on the render thread. that write is the hitch you feel
   a moment after going through a portal. it is only a log, so skip it.
   the per frame counters it resets in onFrameEnd are left alone. */
@Pseudo
@Mixin(targets = "qouteall.imm_ptl.core.render.TeleportFlashProbe", remap = false)
public abstract class PortalFlashProbeMixin {
    @Inject(method = "collectRow", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private static void gc$skipRow(CallbackInfoReturnable<String> cir) {
        if (!PortalDiagnostics.enabled()) {
            cir.setReturnValue("");
        }
    }

    @Inject(method = "dump", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private static void gc$skipDump(CallbackInfo ci) {
        if (!PortalDiagnostics.enabled()) {
            ci.cancel();
        }
    }

    @Inject(method = "insertMarker", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private static void gc$skipMarker(CallbackInfo ci) {
        if (!PortalDiagnostics.enabled()) {
            ci.cancel();
        }
    }
}

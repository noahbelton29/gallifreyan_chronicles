package uk.nbforge.gallifreyan_chronicles.mixin.client.compat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import uk.nbforge.gallifreyan_chronicles.compat.portal.PortalDiagnostics;

/**
 * immersive portals teleport flash debug probe. every frame all session it builds a row of text (a bunch of
 * String.format calls plus fog and sky attribute lookups) for a 96 frame ring. cross a portal and it records 40 more
 * frames then dumps the whole ring, about 32 KB, to the log in one go on the render thread, thats the hitch u feel a
 * moment after going thru. its only a log so skip it. the per frame counters it resets in onFrameEnd are left alone
 */
@Pseudo
@Mixin(targets = "qouteall.imm_ptl.core.render.TeleportFlashProbe", remap = false)
public abstract class PortalFlashProbeMixin {
    @Inject(method = "collectRow", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private static void gc$skipRow(CallbackInfoReturnable<String> cir) {
        if (PortalDiagnostics.disabled()) {
            cir.setReturnValue("");
        }
    }

    @Inject(method = "dump", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private static void gc$skipDump(CallbackInfo ci) {
        if (PortalDiagnostics.disabled()) {
            ci.cancel();
        }
    }

    @Inject(method = "insertMarker", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private static void gc$skipMarker(CallbackInfo ci) {
        if (PortalDiagnostics.disabled()) {
            ci.cancel();
        }
    }
}

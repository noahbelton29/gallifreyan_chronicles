package uk.nbforge.gallifreyan_chronicles.mixin.client.compat;

import uk.nbforge.gallifreyan_chronicles.compat.portal.PortalDiagnostics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/* another immersive portals debug probe. after every crossing it is armed for 1200 frames and
   builds and logs a long status line on the render thread once a second. log only, so skip it */
@Pseudo
@Mixin(targets = "qouteall.imm_ptl.core.render.RenderChainProbe", remap = false)
public abstract class PortalChainProbeMixin {
    @Inject(method = "armOnPromote", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private static void gc$skipArm(CallbackInfo ci) {
        if (!PortalDiagnostics.enabled()) {
            ci.cancel();
        }
    }

    @Inject(method = "onFrame", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private static void gc$skipFrame(CallbackInfo ci) {
        if (!PortalDiagnostics.enabled()) {
            ci.cancel();
        }
    }
}

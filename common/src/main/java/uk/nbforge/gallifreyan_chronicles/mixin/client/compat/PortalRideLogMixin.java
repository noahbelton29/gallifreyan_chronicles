package uk.nbforge.gallifreyan_chronicles.mixin.client.compat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import uk.nbforge.gallifreyan_chronicles.compat.portal.PortalDiagnostics;

/**
 * seamless portals logs every step of a crossing at info for the next 80 ticks, allows a few hundred lines and the
 * callers build the strings first. these two methods are the log only part so skip them
 */
@Pseudo
@Mixin(targets = "com.warwa.seamlessportals.passthrough.SeamRideProbe", remap = false)
public abstract class PortalRideLogMixin {
    @Inject(method = "event", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private static void gc$skipEvent(CallbackInfo ci) {
        if (PortalDiagnostics.disabled()) {
            ci.cancel();
        }
    }

    @Inject(method = "critical", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private static void gc$skipCritical(CallbackInfo ci) {
        if (PortalDiagnostics.disabled()) {
            ci.cancel();
        }
    }
}

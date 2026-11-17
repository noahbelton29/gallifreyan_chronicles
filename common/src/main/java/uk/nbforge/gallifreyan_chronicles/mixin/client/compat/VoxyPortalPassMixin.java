package uk.nbforge.gallifreyan_chronicles.mixin.client.compat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import uk.nbforge.gallifreyan_chronicles.compat.portal.VoxyPortalGuard;

/**
 * Keeps Voxy from breaking the stencil clipping of Immersive Portals views.
 * Voxy draws its far terrain in the middle of the chunk pass so it also draws inside a portal view, like looking out of a TARDIS door at the overworld.
 * At the end it switches the stencil test off and never back on, and Immersive Portals clips the whole portal view with that stencil, so everything drawn after Voxy in that view, like another TARDIS exterior in front of the door, would no longer be clipped to the door opening and would be painted over the interior around it.
 * This saves the stencil state of the portal when Voxy starts and puts it back when done, VoxyPortalBlitMixin does the same for the final composite, see VoxyPortalGuard.
 * this shit only breaks when voxy is installed, don't touch it.
 */
@Pseudo
@Mixin(targets = "me.cortex.voxy.client.core.VoxyRenderSystem", remap = false)
public abstract class VoxyPortalPassMixin {
    // Saves the stencil state of the portal and skips Voxy for this view if the stencil cannot be used safely
    @Inject(method = "renderOpaque", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void gc$enterPortalView(CallbackInfo ci) {
        if (!VoxyPortalGuard.begin()) ci.cancel();
    }

    // Puts the stencil state of the portal back
    @Inject(method = "renderOpaque", at = @At("RETURN"), remap = false, require = 0)
    private void gc$leavePortalView(CallbackInfo ci) {
        VoxyPortalGuard.end();
    }
}

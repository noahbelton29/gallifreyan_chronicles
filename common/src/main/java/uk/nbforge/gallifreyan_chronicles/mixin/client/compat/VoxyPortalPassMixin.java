package uk.nbforge.gallifreyan_chronicles.mixin.client.compat;

import uk.nbforge.gallifreyan_chronicles.compat.portal.VoxyPortalGuard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Keeps Voxy from breaking the stencil clipping of Immersive Portals views.
 *
 * <p>Voxy draws its far terrain in the middle of the chunk pass, so it also draws inside a portal view
 * (looking out of a TARDIS door at the overworld, for example). At the end it switches the stencil test
 * off and never switches it back on. Immersive Portals clips the whole portal view with that stencil, so
 * everything drawn after Voxy in that view (like another TARDIS exterior standing in front of the door)
 * would no longer be clipped to the door opening and would be painted over the interior around it.
 *
 * <p>This saves the portal's stencil state when Voxy starts and puts it back when it is done.
 * {@link VoxyPortalBlitMixin} does the same for Voxy's final composite.
 *
 * @see VoxyPortalGuard
 */
@Pseudo
@Mixin(targets = "me.cortex.voxy.client.core.VoxyRenderSystem", remap = false)
public abstract class VoxyPortalPassMixin {
    /** Saves the portal's stencil state, and skips Voxy for this view if the stencil can't be used safely. */
    @Inject(method = "renderOpaque", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void gc$enterPortalView(CallbackInfo ci) {
        if (!VoxyPortalGuard.begin()) ci.cancel();
    }

    /** Puts the portal's stencil state back. */
    @Inject(method = "renderOpaque", at = @At("RETURN"), remap = false, require = 0)
    private void gc$leavePortalView(CallbackInfo ci) {
        VoxyPortalGuard.end();
    }
}

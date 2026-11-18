package uk.nbforge.gallifreyan_chronicles.mixin.client.compat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import uk.nbforge.gallifreyan_chronicles.compat.portal.VoxyPortalGuard;

/**
 * stops voxy breaking the stencil clipping in immersive portals views. voxy draws its far terrain mid chunk pass so it
 * draws inside portal views too, like looking out a tardis door at the overworld. at the end it turns the stencil test
 * off and never back on, and immersive portals clips the whole portal view with that stencil, so anything drawn after
 * voxy in that view (like another tardis exterior in front of the door) stops being clipped to the opening and paints
 * over the interior around it. this saves the portal's stencil state when voxy starts and puts it back when done,
 * VoxyPortalBlitMixin does the same for the final composite, see VoxyPortalGuard. this shit only breaks when voxy is
 * installed, don't touch it
 */
@Pseudo
@Mixin(targets = "me.cortex.voxy.client.core.VoxyRenderSystem", remap = false)
public abstract class VoxyPortalPassMixin {
    @Inject(method = "renderOpaque", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void gc$enterPortalView(CallbackInfo ci) {
        if (VoxyPortalGuard.beginOrSkip()) ci.cancel();
    }

    @Inject(method = "renderOpaque", at = @At("RETURN"), remap = false, require = 0)
    private void gc$leavePortalView(CallbackInfo ci) {
        VoxyPortalGuard.end();
    }
}

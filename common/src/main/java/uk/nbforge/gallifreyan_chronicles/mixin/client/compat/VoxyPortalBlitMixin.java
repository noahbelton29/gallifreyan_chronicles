package uk.nbforge.gallifreyan_chronicles.mixin.client.compat;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import uk.nbforge.gallifreyan_chronicles.compat.portal.VoxyPortalGuard;
import org.lwjgl.opengl.GL11C;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Clips Voxy's final composite to the Immersive Portals opening.
 *
 * <p>The last step of Voxy's rendering copies its far terrain (colour and depth) onto the game's
 * framebuffer. It does that through a scratch framebuffer that has no stencil buffer, and it turns the
 * stencil test off first. Inside a portal view the terrain is then written over the whole screen and not
 * just the portal opening.
 *
 * <p>In a portal view this attaches the stencil buffer for the copy and leaves the portal's stencil test
 * on, so the copy is clipped to the opening like everything else in that view. Outside portal views it
 * does nothing.
 *
 * @see VoxyPortalGuard
 */
@Pseudo
@Mixin(targets = "me.cortex.voxy.client.core.AbstractRenderPipeline", remap = false)
public abstract class VoxyPortalBlitMixin {
    /** Stops Voxy turning the stencil test off before the composite, when the composite is being clipped. */
    @WrapOperation(method = "transformBlitDepth",
            at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11C;glDisable(I)V", ordinal = 0),
            remap = false, require = 0)
    private static void gc$keepPortalStencil(int capability, Operation<Void> original) {
        if (capability == GL11C.GL_STENCIL_TEST && VoxyPortalGuard.clippingComposite()) return;
        original.call(capability);
    }

    /** Attaches the stencil buffer to Voxy's scratch framebuffer once it is bound. */
    @Inject(method = "transformBlitDepth",
            at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL30C;glBindFramebuffer(II)V", shift = At.Shift.AFTER),
            remap = false, require = 0)
    private static void gc$clipComposite(CallbackInfo ci) {
        VoxyPortalGuard.prepareClippedBlit();
    }

    /** Detaches the stencil buffer again once the composite is done. */
    @Inject(method = "transformBlitDepth", at = @At("RETURN"), remap = false, require = 0)
    private static void gc$unclipComposite(CallbackInfo ci) {
        VoxyPortalGuard.finishClippedBlit();
    }
}

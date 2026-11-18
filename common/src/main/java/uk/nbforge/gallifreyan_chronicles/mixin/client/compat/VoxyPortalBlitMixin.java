package uk.nbforge.gallifreyan_chronicles.mixin.client.compat;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.lwjgl.opengl.GL11C;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import uk.nbforge.gallifreyan_chronicles.compat.portal.VoxyPortalGuard;

/**
 * clips voxy's final composite to the immersive portals opening. voxy's last step copies its far terrain (colour and
 * depth) onto the game framebuffer thru a scratch framebuffer with no stencil buffer, and turns the stencil test off
 * first. so inside a portal view the terrain gets written over the whole screen not just the opening. in a portal view
 * this attaches the stencil buffer for the copy and leaves the portal's stencil test on so the copy gets clipped like
 * everything else. does nothing outside portal views, see VoxyPortalGuard
 */
@Pseudo
@Mixin(targets = "me.cortex.voxy.client.core.AbstractRenderPipeline", remap = false)
public abstract class VoxyPortalBlitMixin {
    @WrapOperation(
            method = "transformBlitDepth",
            at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11C;glDisable(I)V", ordinal = 0),
            remap = false,
            require = 0)
    private static void gc$keepPortalStencil(int capability, Operation<Void> original) {
        if (capability == GL11C.GL_STENCIL_TEST && VoxyPortalGuard.clippingComposite()) return;
        original.call(capability);
    }

    @Inject(
            method = "transformBlitDepth",
            at =
                    @At(
                            value = "INVOKE",
                            target = "Lorg/lwjgl/opengl/GL30C;glBindFramebuffer(II)V",
                            shift = At.Shift.AFTER),
            remap = false,
            require = 0)
    private static void gc$clipComposite(CallbackInfo ci) {
        VoxyPortalGuard.prepareClippedBlit();
    }

    @Inject(method = "transformBlitDepth", at = @At("RETURN"), remap = false, require = 0)
    private static void gc$unclipComposite(CallbackInfo ci) {
        VoxyPortalGuard.finishClippedBlit();
    }
}

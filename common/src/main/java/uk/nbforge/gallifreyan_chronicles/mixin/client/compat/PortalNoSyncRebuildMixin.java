package uk.nbforge.gallifreyan_chronicles.mixin.client.compat;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

/**
 * The first time a portal draws, Immersive Portals made Sodium build every chunk on the main thread in that one frame so the view was not empty, and that is the freeze when a portal shows up.
 * Skip it, the chunks just build in the background instead.
 * this freeze was a nightmare to track down.
 */
@Pseudo
@Mixin(targets = "qouteall.imm_ptl.core.compat.sodium_compatibility.SodiumContextRegistry", remap = false)
public abstract class PortalNoSyncRebuildMixin {
    @WrapOperation(
            method = "acquire",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lqouteall/imm_ptl/core/render/ForceMainThreadRebuild;forceMainThreadRebuildFor(I)V"),
            remap = false,
            require = 0)
    private static void gc$noSyncRebuild(int frames, Operation<Void> original) {}
}

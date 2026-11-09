package com.noahtnt2009.gallifreyan_chronicles.mixin.client.compat;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

/* the first time a portal draws, immersive portals made sodium build every chunk
   on the main thread in that one frame so the view wasn't empty. that's the freeze
   when a portal shows up. skip it, the chunks just build in the background instead */
@Pseudo
@Mixin(targets = "qouteall.imm_ptl.core.compat.sodium_compatibility.SodiumContextRegistry", remap = false)
public abstract class PortalNoSyncRebuildMixin {
    @WrapOperation(method = "acquire",
            at = @At(value = "INVOKE",
                    target = "Lqouteall/imm_ptl/core/render/ForceMainThreadRebuild;forceMainThreadRebuildFor(I)V"),
            remap = false, require = 0)
    private static void gc$noSyncRebuild(int frames, Operation<Void> original) {
    }
}

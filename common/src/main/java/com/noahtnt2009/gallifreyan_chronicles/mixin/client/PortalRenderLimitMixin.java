package com.noahtnt2009.gallifreyan_chronicles.mixin.client;

import com.noahtnt2009.gallifreyan_chronicles.compat.portal.TardisPortalBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import qouteall.imm_ptl.core.portal.Portal;

import java.util.function.Supplier;

@Pseudo
@Mixin(targets = "qouteall.imm_ptl.core.render.renderer.PortalRenderer", remap = false)
public abstract class PortalRenderLimitMixin {
    @Inject(method = "shouldSkipRenderingPortal", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private static void gc$limitTardisPortals(Portal portal, Supplier<?> frustumSupplier,
                                              CallbackInfoReturnable<Boolean> cir) {
        if (TardisPortalBridge.shouldSkipPortalRendering(portal)) {
            cir.setReturnValue(true);
        }
    }
}
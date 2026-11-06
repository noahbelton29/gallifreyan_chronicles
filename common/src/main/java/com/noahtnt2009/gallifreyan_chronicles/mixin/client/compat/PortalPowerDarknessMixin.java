package com.noahtnt2009.gallifreyan_chronicles.mixin.client.compat;

import com.noahtnt2009.gallifreyan_chronicles.compat.portal.TardisPortalBridge;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import qouteall.imm_ptl.core.portal.Portal;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "qouteall.imm_ptl.core.render.renderer.RendererUsingStencil", remap = false)
public abstract class PortalPowerDarknessMixin {
    @Inject(method = "doRenderPortal",
            at = @At(value = "INVOKE",
                    target = "Lqouteall/imm_ptl/core/render/renderer/RendererUsingStencil;clampStencilValue(I)V",
                    ordinal = 1),
            remap = false, require = 0)
    private void gc$tintPoweredDownInterior(Portal portal, Matrix4f modelView, CallbackInfo ci) {
        TardisPortalBridge.tintPortalForPowerDarkness(portal);
    }
}

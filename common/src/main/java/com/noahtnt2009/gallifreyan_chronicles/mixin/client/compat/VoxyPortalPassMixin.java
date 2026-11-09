package com.noahtnt2009.gallifreyan_chronicles.mixin.client.compat;

import com.noahtnt2009.gallifreyan_chronicles.compat.portal.TardisPortalBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/* voxy draws its far terrain using the main camera. inside an immersive portals
   view (looking through a door) that put it in the wrong place and covered things
   in a big black shape. so voxy only draws in the normal view, not through portals */
@Pseudo
@Mixin(targets = "me.cortex.voxy.client.core.VoxyRenderSystem", remap = false)
public abstract class VoxyPortalPassMixin {
    @Inject(method = "renderOpaque", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void gc$skipInPortalView(CallbackInfo ci) {
//        if (TardisPortalBridge.isRenderingAnyPortal()) ci.cancel();
    }
}

package com.noahtnt2009.gallifreyan_chronicles.mixin.client.compat;

import com.noahtnt2009.gallifreyan_chronicles.compat.flashback.FlashbackChunks;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/* immersive portals counts the portals it drew this frame and stops at a limit.
   the count is reset at the start of each normal frame, but a flashback export
   draws its frames another way that skips that reset. so the count kept going up,
   and a few seconds in it hit the limit and every portal stopped drawing.
   reset it at the start of each frame in a replay */
@Mixin(GameRenderer.class)
public abstract class ExportPortalCountMixin {
    @Inject(method = "update", at = @At("HEAD"), require = 0)
    private void gc$resetPortalCount(DeltaTracker deltaTracker, CallbackInfo ci) {
        FlashbackChunks.newExportFrame();
    }
}

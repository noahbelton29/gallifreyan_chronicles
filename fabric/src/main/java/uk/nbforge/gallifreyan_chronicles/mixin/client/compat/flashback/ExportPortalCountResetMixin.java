package uk.nbforge.gallifreyan_chronicles.mixin.client.compat.flashback;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import uk.nbforge.gallifreyan_chronicles.compat.flashback.FlashbackChunks;

/**
 * Immersive Portals counts the portals it drew this frame and stops at a limit.
 * The count is reset at the start of each normal frame but an export of Flashback draws its frames another way that skips that reset, so the count kept going up and a few seconds in it hit the limit and every portal stopped drawing.
 * Reset it at the start of each frame in a replay.
 * took me way too long to work out why portals just died after a few seconds.
 */
@Mixin(GameRenderer.class)
public abstract class ExportPortalCountResetMixin {
    @Inject(method = "update", at = @At("HEAD"), require = 0)
    private void gc$resetPortalCount(DeltaTracker deltaTracker, CallbackInfo ci) {
        FlashbackChunks.newExportFrame();
    }
}

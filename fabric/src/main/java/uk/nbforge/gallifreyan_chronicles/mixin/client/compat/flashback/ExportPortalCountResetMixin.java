package uk.nbforge.gallifreyan_chronicles.mixin.client.compat.flashback;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import uk.nbforge.gallifreyan_chronicles.compat.flashback.FlashbackChunks;

/**
 * immersive portals counts the portals it drew this frame and stops at a limit. the count resets at the start of each
 * normal frame but a flashback export draws its frames another way that skips that reset, so the count kept climbing
 * and a few seconds in it hit the limit and every portal stopped drawing. reset it every frame in a replay. took me way
 * too long to work out why portals just died after a few seconds
 */
@Mixin(GameRenderer.class)
public abstract class ExportPortalCountResetMixin {
    @Inject(method = "update", at = @At("HEAD"), require = 0)
    private void gc$resetPortalCount(DeltaTracker deltaTracker, CallbackInfo ci) {
        FlashbackChunks.newExportFrame();
    }
}

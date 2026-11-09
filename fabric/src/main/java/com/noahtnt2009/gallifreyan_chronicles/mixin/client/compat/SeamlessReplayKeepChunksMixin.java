package com.noahtnt2009.gallifreyan_chronicles.mixin.client.compat;

import com.noahtnt2009.gallifreyan_chronicles.compat.flashback.FlashbackChunks;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/* seamless portals has its own client chunk store next to immersive portals' one,
   and it throws away chunks far from where it thinks the portal view is. once the
   recorded player walked into the tardis that moved, the interior chunks got thrown
   away and the door showed empty sky. in a replay nothing sends them again, so keep them */
@Pseudo
@Mixin(targets = "com.warwa.seamlessportals.client.SeamlessClientChunkMap", remap = false)
public abstract class SeamlessReplayKeepChunksMixin {
    @Inject(method = "drop", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void gc$keepDrop(ChunkPos pos, CallbackInfo ci) {
        if (FlashbackChunks.keepChunk("seamless drop")) ci.cancel();
    }

    @Inject(method = "seamlessportals$evictAround", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void gc$keepAround(int x, int z, int radius, CallbackInfo ci) {
        if (FlashbackChunks.keepChunk("seamless evictAround")) ci.cancel();
    }

    @Inject(method = "seamlessportals$evictBeyond", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void gc$keepBeyond(int radius, CallbackInfo ci) {
        if (FlashbackChunks.keepChunk("seamless evictBeyond")) ci.cancel();
    }

    @Inject(method = "seamlessportals$evictAll", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void gc$keepAll(CallbackInfo ci) {
        if (FlashbackChunks.keepChunk("seamless evictAll")) ci.cancel();
    }
}

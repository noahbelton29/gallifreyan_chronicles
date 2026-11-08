package com.noahtnt2009.gallifreyan_chronicles.mixin.client.compat;

import com.noahtnt2009.gallifreyan_chronicles.compat.flashback.FlashbackChunks;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/* in a replay the client must never throw its chunks away,
   nothing sends them again. see FlashbackChunks.keepChunk */
@Pseudo
@Mixin(targets = "qouteall.imm_ptl.core.chunk_loading.ImmPtlClientChunkMap", remap = false)
public abstract class ImmPtlReplayKeepChunksMixin {
    @Inject(method = "drop", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void gc$keepDrop(ChunkPos pos, CallbackInfo ci) {
        if (FlashbackChunks.keepChunk("drop")) ci.cancel();
    }

    @Inject(method = "evictBeyond", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void gc$keepBeyond(int radius, CallbackInfo ci) {
        if (FlashbackChunks.keepChunk("evictBeyond")) ci.cancel();
    }

    @Inject(method = "evictAround", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void gc$keepAround(int x, int z, int radius, CallbackInfo ci) {
        if (FlashbackChunks.keepChunk("evictAround")) ci.cancel();
    }

    @Inject(method = "evictAll", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void gc$keepAll(CallbackInfo ci) {
        if (FlashbackChunks.keepChunk("evictAll")) ci.cancel();
    }
}

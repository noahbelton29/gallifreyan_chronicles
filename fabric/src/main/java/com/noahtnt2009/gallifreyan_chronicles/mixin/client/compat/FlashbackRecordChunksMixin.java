package com.noahtnt2009.gallifreyan_chronicles.mixin.client.compat;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import qouteall.imm_ptl.core.chunk_loading.ImmPtlClientChunkMap;

import java.util.List;
import java.util.concurrent.atomic.AtomicReferenceArray;

/* when you start recording, flashback saves the chunks that are already loaded.
   it reads them from the vanilla chunk array, but immersive portals keeps
   its chunks somewhere else, so that array is empty and the replay had no world.
   here we give flashback immersive portals' chunks instead. */
@Pseudo
@Mixin(targets = "com.moulberry.flashback.record.Recorder", remap = false)
public abstract class FlashbackRecordChunksMixin {
    @WrapOperation(method = "writeSnapshot",
            at = @At(value = "FIELD", opcode = 180,
                    target = "Lnet/minecraft/client/multiplayer/ClientChunkCache$Storage;chunks:Ljava/util/concurrent/atomic/AtomicReferenceArray;"),
            remap = false, require = 0)
    private AtomicReferenceArray<LevelChunk> gc$immPtlChunks(@Coerce Object storage,
                                                             Operation<AtomicReferenceArray<LevelChunk>> original) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null && level.getChunkSource() instanceof ImmPtlClientChunkMap map) {
            List<LevelChunk> chunks = map.getCopiedChunkList();
            AtomicReferenceArray<LevelChunk> array = new AtomicReferenceArray<>(chunks.size());
            for (int i = 0; i < chunks.size(); i++) array.set(i, chunks.get(i));
            return array;
        }
        return original.call(storage);
    }
}

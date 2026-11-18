package uk.nbforge.gallifreyan_chronicles.mixin.client.compat.flashback;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.util.List;
import java.util.concurrent.atomic.AtomicReferenceArray;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import qouteall.imm_ptl.core.chunk_loading.ImmPtlClientChunkMap;
import uk.nbforge.gallifreyan_chronicles.util.GCLog;

/**
 * when u start recording flashback saves the chunks that are already loaded. it reads them from the vanilla chunk array
 * but immersive portals keeps its chunks somewhere else, so that array is empty and the replay had no world. flashback
 * gets the immersive portals chunks instead
 */
@Pseudo
@Mixin(targets = "com.moulberry.flashback.record.Recorder", remap = false)
public abstract class RecordImmPtlChunksMixin {
    @WrapOperation(
            method = "writeSnapshot",
            at =
                    @At(
                            value = "FIELD",
                            opcode = 180,
                            target =
                                    "Lnet/minecraft/client/multiplayer/ClientChunkCache$Storage;chunks:Ljava/util/concurrent/atomic/AtomicReferenceArray;"),
            remap = false,
            require = 0)
    private AtomicReferenceArray<LevelChunk> gc$immPtlChunks(
            @Coerce Object storage, Operation<AtomicReferenceArray<LevelChunk>> original) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null && level.getChunkSource() instanceof ImmPtlClientChunkMap map) {
            List<LevelChunk> chunks = map.getCopiedChunkList();
            int air = 0;
            for (LevelChunk c : chunks) {
                boolean solid = false;
                for (var sec : c.getSections())
                    if (!sec.hasOnlyAir()) {
                        solid = true;
                        break;
                    }
                if (!solid) air++;
            }
            GCLog.REPLAY.info(
                    "Record snapshot: {} chunks, {} all-air, level={}",
                    chunks.size(),
                    air,
                    level.dimension().identifier());
            AtomicReferenceArray<LevelChunk> array = new AtomicReferenceArray<>(chunks.size());
            for (int i = 0; i < chunks.size(); i++) array.set(i, chunks.get(i));
            return array;
        }
        return original.call(storage);
    }
}

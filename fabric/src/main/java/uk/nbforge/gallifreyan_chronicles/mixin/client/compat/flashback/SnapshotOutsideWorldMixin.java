package uk.nbforge.gallifreyan_chronicles.mixin.client.compat.flashback;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import uk.nbforge.gallifreyan_chronicles.compat.flashback.FlashbackChunks;

/**
 * Flashback saves a snapshot of the world every so often while recording.
 * One taken while you stood in the TARDIS was a snapshot of the TARDIS world so going back on the timeline loaded the TARDIS as the world of the replay, with loading world and the camera stuck in the interior.
 * Now snapshots are always taken from the outside world you came from, with the TARDIS saved behind its door like everything else.
 */
@Pseudo
@Mixin(targets = "com.moulberry.flashback.record.Recorder", remap = false)
public abstract class SnapshotOutsideWorldMixin {
    @WrapMethod(method = "writeSnapshot", remap = false, require = 0)
    private void gc$fromOutside(boolean asActualSnapshot, Operation<Void> original) {
        FlashbackChunks.writeSnapshotFromOutside(() -> original.call(asActualSnapshot));
    }
}

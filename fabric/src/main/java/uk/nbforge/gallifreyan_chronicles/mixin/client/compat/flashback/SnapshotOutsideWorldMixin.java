package uk.nbforge.gallifreyan_chronicles.mixin.client.compat.flashback;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import uk.nbforge.gallifreyan_chronicles.compat.flashback.FlashbackChunks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

/* flashback saves a snapshot of the world every so often while recording. one taken
   while you stood in the tardis was a snapshot of the tardis world, so going back on
   the timeline loaded the tardis as the replay's world ("loading world", camera stuck
   in the interior). now snapshots are always taken from the outside world you came
   from, with the tardis saved behind its door like everything else */
@Pseudo
@Mixin(targets = "com.moulberry.flashback.record.Recorder", remap = false)
public abstract class SnapshotOutsideWorldMixin {
    @WrapMethod(method = "writeSnapshot", remap = false, require = 0)
    private void gc$fromOutside(boolean asActualSnapshot, Operation<Void> original) {
        FlashbackChunks.writeSnapshotFromOutside(() -> original.call(asActualSnapshot));
    }
}

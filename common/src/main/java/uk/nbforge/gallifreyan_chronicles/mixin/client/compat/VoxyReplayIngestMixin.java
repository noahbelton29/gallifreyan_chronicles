package uk.nbforge.gallifreyan_chronicles.mixin.client.compat;

import uk.nbforge.gallifreyan_chronicles.compat.replay.PortalReplay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
   Voxy learns far terrain from every chunk the game loads.
   In a replay with Immersive Portals the chunks come in odd ways, from other worlds, kept chunks and snapshot extras, and that messed up the terrain of Voxy.
   So in those replays Voxy only shows what it already knows and does not learn from the replay.
*/
@Pseudo
@Mixin(targets = "me.cortex.voxy.common.world.service.VoxelIngestService", remap = false)
public abstract class VoxyReplayIngestMixin {
    @Inject(method = "enqueueIngest", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void gc$noIngest(CallbackInfoReturnable<Boolean> cir) {
        if (PortalReplay.active()) cir.setReturnValue(false);
    }

    @Inject(method = "rawIngest(Lme/cortex/voxy/commonImpl/WorldIdentifier;Lnet/minecraft/world/level/chunk/LevelChunkSection;IIILnet/minecraft/world/level/chunk/DataLayer;Lnet/minecraft/world/level/chunk/DataLayer;)Z",
            at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private static void gc$noRawIngestId(CallbackInfoReturnable<Boolean> cir) {
        if (PortalReplay.active()) cir.setReturnValue(false);
    }

    @Inject(method = "rawIngest(Lme/cortex/voxy/common/world/WorldEngine;Lnet/minecraft/world/level/chunk/LevelChunkSection;IIILnet/minecraft/world/level/chunk/DataLayer;Lnet/minecraft/world/level/chunk/DataLayer;)Z",
            at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private static void gc$noRawIngestEngine(CallbackInfoReturnable<Boolean> cir) {
        if (PortalReplay.active()) cir.setReturnValue(false);
    }
}

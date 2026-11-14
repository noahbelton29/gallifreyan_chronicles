package uk.nbforge.gallifreyan_chronicles.mixin.client.compat;

import uk.nbforge.gallifreyan_chronicles.compat.replay.PortalReplay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/* voxy learns far terrain from every chunk the game loads. in a replay with
   immersive portals the chunks come in odd ways (other worlds, kept chunks,
   snapshot extras) and that messed up voxy's terrain. so in those replays voxy
   only shows what it already knows and doesn't learn from the replay */
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

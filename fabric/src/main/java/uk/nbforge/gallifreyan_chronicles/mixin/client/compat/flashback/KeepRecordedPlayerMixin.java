package uk.nbforge.gallifreyan_chronicles.mixin.client.compat.flashback;

import uk.nbforge.gallifreyan_chronicles.compat.flashback.FlashbackChunks;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/* in a replay, immersive portals can send a "remove entity" for the recorded
   player (it used the same id) and the player vanished for the rest of the replay.
   keep the player. see FlashbackChunks.keepPlayer.
   but when the player is added again (jumping on the timeline) the game first
   removes the old one with the same id. that removal must go through, or the
   old player stays stuck where it was */
@Mixin(ClientLevel.class)
public abstract class KeepRecordedPlayerMixin {
    @Unique
    private static int gc$addingId = Integer.MIN_VALUE;

    @Inject(method = "addEntity", at = @At("HEAD"), require = 0)
    private void gc$startAdd(Entity entity, CallbackInfo ci) {
        gc$addingId = entity.getId();
    }

    @Inject(method = "addEntity", at = @At("RETURN"), require = 0)
    private void gc$endAdd(Entity entity, CallbackInfo ci) {
        gc$addingId = Integer.MIN_VALUE;
    }

    @Inject(method = "removeEntity", at = @At("HEAD"), cancellable = true, require = 0)
    private void gc$keepPlayer(int id, Entity.RemovalReason reason, CallbackInfo ci) {
        if (id == gc$addingId) return; /* replacing it with the new one */
        if (FlashbackChunks.keepPlayer((ClientLevel) (Object) this, id)) ci.cancel();
    }
}

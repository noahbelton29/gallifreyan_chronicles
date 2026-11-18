package uk.nbforge.gallifreyan_chronicles.mixin.client.compat.flashback;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import uk.nbforge.gallifreyan_chronicles.compat.flashback.FlashbackChunks;

/**
 * in a replay immersive portals can send a remove entity for the recorded player cuz it used the same id, and the
 * player vanished for the rest of the replay, so keep them (see FlashbackChunks keepPlayer). but when the player gets
 * added again by jumping on the timeline the game removes the old one with the same id first, that removal has to go
 * thru or the old player stays stuck where it was
 */
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
        if (id == gc$addingId) return;
        if (FlashbackChunks.keepPlayer((ClientLevel) (Object) this, id)) ci.cancel();
    }
}

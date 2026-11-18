package uk.nbforge.gallifreyan_chronicles.mixin.client.audio;

import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import uk.nbforge.gallifreyan_chronicles.client.sound.SoundListener;

/**
 * in a replay only the world the camera is in makes sounds. the tardis world stays loaded behind the door and its
 * sounds (controls, doors, flight) were being heard from the overworld. outside hum and power sounds dont go thru here
 */
@Mixin(ClientLevel.class)
public abstract class ReplaySoundWorldMixin {
    @Inject(
            method = {"playSeededSound*", "playLocalSound*", "playPlayerSound*"},
            at = @At("HEAD"),
            cancellable = true,
            require = 0)
    private void gc$onlyHeardWorld(CallbackInfo ci) {
        if (SoundListener.inReplay() && (Object) this != SoundListener.level()) ci.cancel();
    }
}

package com.noahtnt2009.gallifreyan_chronicles.mixin.client.compat;

import com.noahtnt2009.gallifreyan_chronicles.compat.flashback.FlashbackChunks;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/* in a replay, the recorded player we hid at the tardis door kept making sprint
   particles there. the copy inside makes them instead. see FlashbackPuppet */
@Mixin(Entity.class)
public abstract class ReplaySprintParticlesMixin {
    @Inject(method = "canSpawnSprintParticle", at = @At("HEAD"), cancellable = true, require = 0)
    private void gc$noParticlesWhenHidden(CallbackInfoReturnable<Boolean> cir) {
        if (FlashbackChunks.isHiddenRecordedPlayer((Entity) (Object) this)) cir.setReturnValue(false);
    }
}

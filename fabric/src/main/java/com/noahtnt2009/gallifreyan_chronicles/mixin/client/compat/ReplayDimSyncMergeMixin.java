package com.noahtnt2009.gallifreyan_chronicles.mixin.client.compat;

import com.noahtnt2009.gallifreyan_chronicles.compat.flashback.FlashbackChunks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/* after the client takes a dimension list, in a replay merge it with the ones
   from before. see FlashbackDimMerge */
@Pseudo
@Mixin(targets = "qouteall.q_misc_util.MiscNetworking$DimIdSyncPacket", remap = false)
public abstract class ReplayDimSyncMergeMixin {
    @Inject(method = "handle", at = @At("RETURN"), remap = false, require = 0)
    private void gc$merge(CallbackInfo ci) {
        FlashbackChunks.afterDimSync(this);
    }
}

package com.noahtnt2009.gallifreyan_chronicles.mixin.client.compat;

import com.noahtnt2009.gallifreyan_chronicles.compat.flashback.FlashbackChunks;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/* in a replay, remove portals that show up with no shape. see FlashbackChunks */
@Mixin(ClientLevel.class)
public abstract class ReplayShapelessPortalMixin {
    @Inject(method = "addEntity", at = @At("TAIL"), require = 0)
    private void gc$dropShapeless(Entity entity, CallbackInfo ci) {
        FlashbackChunks.dropShapelessPortal(entity);
    }
}

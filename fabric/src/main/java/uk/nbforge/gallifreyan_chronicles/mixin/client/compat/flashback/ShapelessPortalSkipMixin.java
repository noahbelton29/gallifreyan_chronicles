package uk.nbforge.gallifreyan_chronicles.mixin.client.compat.flashback;

import uk.nbforge.gallifreyan_chronicles.compat.flashback.FlashbackChunks;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
   In a replay, portals that show up with no shape are never added.
   Removing them after adding made Seamless Portals crash in its cleanup because unitAxis is null.
*/
@Mixin(ClientLevel.class)
public abstract class ShapelessPortalSkipMixin {
    @Inject(method = "addEntity", at = @At("HEAD"), cancellable = true, require = 0)
    private void gc$skipShapeless(Entity entity, CallbackInfo ci) {
        if (FlashbackChunks.isShapelessPortalInReplay(entity)) ci.cancel();
    }
}

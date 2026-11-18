package uk.nbforge.gallifreyan_chronicles.mixin.client.compat.flashback;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import uk.nbforge.gallifreyan_chronicles.compat.flashback.FlashbackChunks;

/**
 * in a replay shapeless portals never get added. removing them after adding made seamless portals crash in its cleanup
 * cuz unitAxis is null
 */
@Mixin(ClientLevel.class)
public abstract class ShapelessPortalSkipMixin {
    @Inject(method = "addEntity", at = @At("HEAD"), cancellable = true, require = 0)
    private void gc$skipShapeless(Entity entity, CallbackInfo ci) {
        if (FlashbackChunks.isShapelessPortalInReplay(entity)) ci.cancel();
    }
}

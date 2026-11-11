package com.noahtnt2009.gallifreyan_chronicles.mixin.client.compat;

import com.noahtnt2009.gallifreyan_chronicles.util.TardisDimensions;
import com.noahtnt2009.gallifreyan_chronicles.Constants;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/* voxy should leave tardis dimensions alone */
@Pseudo
@Mixin(targets = "me.cortex.voxy.commonImpl.WorldIdentifier", remap = false)
public abstract class VoxyTardisLodMixin {
    @Inject(method = "of(Lnet/minecraft/world/level/Level;)Lme/cortex/voxy/commonImpl/WorldIdentifier;",
            at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private static void gc$noTardisLods(Level level, CallbackInfoReturnable<Object> cir) {
        if (TardisDimensions.isInterior(level)) {
            cir.setReturnValue(null);
        }
    }
}

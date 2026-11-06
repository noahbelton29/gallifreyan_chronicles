package com.noahtnt2009.gallifreyan_chronicles.mixin.client.compat;

import com.noahtnt2009.gallifreyan_chronicles.client.sky.SkyShaderCompat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "net.irisshaders.iris.Iris", remap = false)
public abstract class IrisDimensionMixin {
    @Inject(method = "getCurrentDimension", at = @At("RETURN"), cancellable = true, remap = false, require = 0)
    private static void gc$customSkyDimension(CallbackInfoReturnable<Object> cir) {
        Object remapped = SkyShaderCompat.remapDimension(cir.getReturnValue());
        if (remapped != null) {
            cir.setReturnValue(remapped);
        }
    }
}

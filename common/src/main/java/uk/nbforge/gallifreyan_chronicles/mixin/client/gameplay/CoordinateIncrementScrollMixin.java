package uk.nbforge.gallifreyan_chronicles.mixin.client.gameplay;

import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import uk.nbforge.gallifreyan_chronicles.client.tardis.CoordinateIncrementScroll;

@Mixin(MouseHandler.class)
public abstract class CoordinateIncrementScrollMixin {
    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void gallifreyan_chronicles$fineTuneIncrement(
            long window, double scrollX, double scrollY, CallbackInfo ci) {
        if (CoordinateIncrementScroll.onScroll(scrollY)) ci.cancel();
    }
}

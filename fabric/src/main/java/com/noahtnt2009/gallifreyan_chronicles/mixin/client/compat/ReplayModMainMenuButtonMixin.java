package com.noahtnt2009.gallifreyan_chronicles.mixin.client.compat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "com.replaymod.replay.handler.GuiHandler", remap = false)
public abstract class ReplayModMainMenuButtonMixin {

    @Inject(method = "injectIntoMainMenu", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void gc$noTitleScreenButton(CallbackInfo ci) {
        ci.cancel();
    }
}

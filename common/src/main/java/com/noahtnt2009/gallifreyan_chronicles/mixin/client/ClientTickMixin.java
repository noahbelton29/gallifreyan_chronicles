package com.noahtnt2009.gallifreyan_chronicles.mixin.client;

import com.noahtnt2009.gallifreyan_chronicles.client.GCClientHooks;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class ClientTickMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void gc$clientTick(CallbackInfo ci) {
        GCClientHooks.clientTick();
    }
}

package com.noahtnt2009.gallifreyan_chronicles.mixin.client.compat;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.noahtnt2009.gallifreyan_chronicles.compat.flashback.FlashbackChunks;

/* when you jump back in a replay, flashback removes every entity one by one.
   removing a portal also removes its linked portals, which broke flashback's
   loop and kicked you out. so we loop over a copy of the list instead */
@Pseudo
@Mixin(targets = "com.moulberry.flashback.Flashback", remap = false)
public abstract class FlashbackClearEntitiesMixin {
    @WrapOperation(method = "lambda$onInitializeClient$3",
            at = @At(value = "INVOKE", target = "Ljava/lang/Iterable;iterator()Ljava/util/Iterator;"),
            remap = false, require = 0)
    private static Iterator<?> gc$copyFirst(Iterable<?> entities, Operation<Iterator<?>> original) {
        List<Object> copy = new ArrayList<>();
        original.call(entities).forEachRemaining(copy::add);
        return copy.iterator();
    }

    /* also drop tardis copy of the player */
    @Inject(method = "lambda$onInitializeClient$3", at = @At("HEAD"), remap = false, require = 0)
    private static void gc$clearPuppet(CallbackInfo ci) {
        FlashbackChunks.clearPuppet();
    }
}

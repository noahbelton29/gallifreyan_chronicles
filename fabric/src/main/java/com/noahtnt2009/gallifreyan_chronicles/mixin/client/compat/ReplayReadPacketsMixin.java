package com.noahtnt2009.gallifreyan_chronicles.mixin.client.compat;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.noahtnt2009.gallifreyan_chronicles.compat.flashback.ReplayComponentIds;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/* marks when flashback's replay server is reading recorded packets, and forgets
   the recording's component numbers when the replay closes. see ReplayComponentIds */
@Pseudo
@Mixin(targets = "com.moulberry.flashback.playback.ReplayServer", remap = false)
public abstract class ReplayReadPacketsMixin {
    @WrapMethod(method = "handleGamePacket", remap = false, require = 0)
    private void gc$reading(RegistryFriendlyByteBuf buf, Operation<Void> original) {
        ReplayComponentIds.startReading();
        try {
            original.call(buf);
        } finally {
            ReplayComponentIds.stopReading();
        }
    }

    @Inject(method = "stopServer", at = @At("HEAD"), remap = false, require = 0)
    private void gc$forget(CallbackInfo ci) {
        ReplayComponentIds.clear();
    }
}

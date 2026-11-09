package com.noahtnt2009.gallifreyan_chronicles.mixin.client.compat;

import com.noahtnt2009.gallifreyan_chronicles.compat.flashback.ReplayComponentIds;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/* the replay found the recording's component numbers: use them, don't pass them on */
@Pseudo
@Mixin(targets = "com.moulberry.flashback.playback.ReplayGamePacketHandler", remap = false)
public abstract class ReplayComponentIdsPayloadMixin {
    @Inject(method = "handleCustomPayload", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void gc$useIds(ClientboundCustomPayloadPacket packet, CallbackInfo ci) {
        if (packet.payload() instanceof ReplayComponentIds.Payload ids) {
            ReplayComponentIds.use(ids);
            ci.cancel();
        }
    }
}

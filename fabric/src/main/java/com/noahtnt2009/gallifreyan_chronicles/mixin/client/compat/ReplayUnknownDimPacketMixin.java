package com.noahtnt2009.gallifreyan_chronicles.mixin.client.compat;

import com.noahtnt2009.gallifreyan_chronicles.compat.flashback.FlashbackChunks;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/* when the replay jumps to the tardis world, packets for it can come before the client
   is told about it. that threw "Missing Dimension" and kicked you out of the replay */
@Pseudo
@Mixin(targets = "qouteall.imm_ptl.core.network.PacketRedirectionClient", remap = false)
public abstract class ReplayUnknownDimPacketMixin {
    @Inject(method = "handleRedirectedPacket", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private static void gc$dropUnknown(int dimId, Packet<ClientGamePacketListener> packet,
                                       ClientGamePacketListener listener, CallbackInfo ci) {
        if (FlashbackChunks.dropUnknownDim(dimId)) ci.cancel();
    }
}

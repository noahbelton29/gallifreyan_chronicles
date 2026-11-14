package uk.nbforge.gallifreyan_chronicles.mixin.client.compat.immersive_portals;

import uk.nbforge.gallifreyan_chronicles.compat.flashback.FlashbackChunks;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientCommonPacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import qouteall.imm_ptl.core.network.PacketRedirection;

/* the recording has packets for other worlds (the tardis inside) wrapped up by
   immersive portals. when the replay server read one, immersive portals unwrapped it
   right there and fed it to the replay server as if it was for the replay's world.
   so tardis chunks were put into the overworld and sent as overworld chunks */
@Pseudo
@Mixin(targets = "qouteall.imm_ptl.core.network.PacketRedirectionClient", remap = false)
public abstract class RedirectedPacketForwardMixin {
    @Inject(method = "handleAtPacketHandle", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private static void gc$passOn(PacketRedirection.Payload payload, ClientCommonPacketListener listener,
                                  Packet<ClientCommonPacketListener> packet, CallbackInfo ci) {
        if (FlashbackChunks.passOnFromReplayServer(listener, payload.dimensionIntId(), packet)) ci.cancel();
    }
}

package uk.nbforge.gallifreyan_chronicles.mixin.client.compat.flashback;

import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import uk.nbforge.gallifreyan_chronicles.compat.flashback.FlashbackChunks;

/** In a replay, do not tell the replay server that the camera went through a door or where it moves while it is in the TARDIS world, see ReplayVirtualDimension. */
@Mixin(ClientCommonPacketListenerImpl.class)
public abstract class VirtualDimensionOutgoingMixin {
    @Inject(
            method = "send(Lnet/minecraft/network/protocol/Packet;)V",
            at = @At("HEAD"),
            cancellable = true,
            require = 0)
    private void gc$dropInReplay(Packet<?> packet, CallbackInfo ci) {
        if (FlashbackChunks.dropOutgoing(packet)) ci.cancel();
    }
}

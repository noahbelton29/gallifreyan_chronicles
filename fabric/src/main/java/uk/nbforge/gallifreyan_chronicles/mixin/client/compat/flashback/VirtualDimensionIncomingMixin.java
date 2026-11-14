package uk.nbforge.gallifreyan_chronicles.mixin.client.compat.flashback;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import uk.nbforge.gallifreyan_chronicles.compat.flashback.FlashbackChunks;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/* while the replay camera is in the tardis world, packets from the replay server
   still belong to its world (the overworld). see ReplayVirtualDimension */
@Mixin(targets = "net.minecraft.network.PacketProcessor$ListenerAndPacket")
public abstract class VirtualDimensionIncomingMixin {
    @WrapOperation(method = "handle",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/network/protocol/Packet;handle(Lnet/minecraft/network/PacketListener;)V"),
            require = 0)
    private void gc$serverWorld(Packet<PacketListener> packet, PacketListener listener, Operation<Void> original) {
        if (!FlashbackChunks.handleIncoming(packet, () -> original.call(packet, listener))) {
            original.call(packet, listener);
        }
    }
}

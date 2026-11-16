package uk.nbforge.gallifreyan_chronicles.mixin.client.compat.flashback;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

/**
 * Same as EntityDataSkipMixin but on the replay server of Flashback.
 * Entity ids can get mixed up, like the data of a TARDIS key landing on another entity, and that crashed the whole replay so skip that one packet instead.
 */
@Pseudo
@Mixin(targets = "com.moulberry.flashback.playback.ReplayGamePacketHandler", remap = false)
public abstract class ServerEntityDataSkipMixin {

    @WrapMethod(method = "handleSetEntityData", remap = false, require = 0)
    private void gc$safeEntityData(ClientboundSetEntityDataPacket packet, Operation<Void> original) {
        try {
            original.call(packet);
        } catch (IndexOutOfBoundsException | ClassCastException | IllegalStateException e) {
        }
    }
}

package uk.nbforge.gallifreyan_chronicles.mixin.client.compat.flashback;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import org.spongepowered.asm.mixin.Mixin;
import uk.nbforge.gallifreyan_chronicles.compat.flashback.FlashbackChunks;

/**
 * In a replay entity ids can get mixed up between worlds so entity data can land on the wrong entity and kick you out, in a replay we just skip it.
 * not pretty but it stops the crash.
 */
@Mixin(ClientPacketListener.class)
public abstract class EntityDataSkipMixin {

    @WrapMethod(method = "handleSetEntityData", require = 0)
    private void gc$safeEntityData(ClientboundSetEntityDataPacket packet, Operation<Void> original) {
        try {
            original.call(packet);
        } catch (IndexOutOfBoundsException | ClassCastException | IllegalStateException e) {
            if (!FlashbackChunks.inReplay()) throw e;
        }
    }
}

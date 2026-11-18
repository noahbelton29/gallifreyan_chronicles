package uk.nbforge.gallifreyan_chronicles.mixin.client.compat.flashback;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundForgetLevelChunkPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import uk.nbforge.gallifreyan_chronicles.compat.flashback.FlashbackChunks;

/**
 * the recording has forget chunk packets in it from immersive portals. in a replay nothing sends those chunks again and
 * the packet also turns the light off for them so they stop drawing, so in a replay it gets skipped. thanks for that
 * immersive portals, really helpful
 */
@Mixin(ClientPacketListener.class)
public abstract class ForgetChunkSkipMixin {
    @Inject(method = "handleForgetLevelChunk", at = @At("HEAD"), cancellable = true, require = 0)
    private void gc$skipForget(ClientboundForgetLevelChunkPacket packet, CallbackInfo ci) {
        if (FlashbackChunks.skipForget()) ci.cancel();
    }
}

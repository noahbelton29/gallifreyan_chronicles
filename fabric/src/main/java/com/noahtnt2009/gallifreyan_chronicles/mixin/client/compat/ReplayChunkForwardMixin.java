package com.noahtnt2009.gallifreyan_chronicles.mixin.client.compat;

import com.noahtnt2009.gallifreyan_chronicles.compat.flashback.FlashbackChunks;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/* flashback has just put a recorded chunk in the replay world.
   with immersive portals nothing sends it to the viewer, so we do. see FlashbackChunks */
@Pseudo
@Mixin(targets = "com.moulberry.flashback.playback.ReplayGamePacketHandler", remap = false)
public abstract class ReplayChunkForwardMixin {
    @Shadow
    public abstract ServerLevel level();

    @Inject(method = "handleLevelChunkWithLight", at = @At("TAIL"), remap = false, require = 0)
    private void gc$forwardChunk(ClientboundLevelChunkWithLightPacket packet, CallbackInfo ci) {
        FlashbackChunks.forward(this.level(), packet);
    }
}

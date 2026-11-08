package com.noahtnt2009.gallifreyan_chronicles.mixin.client.compat;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.noahtnt2009.gallifreyan_chronicles.compat.flashback.FlashbackChunks;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

/* before flashback moves the recorded entities, we take a look at the data
   (to follow the recorded player into a tardis). see FlashbackPuppet */
@Pseudo
@Mixin(targets = "com.moulberry.flashback.playback.ReplayServer", remap = false)
public abstract class ReplayPlayerDimensionMixin {
    @WrapMethod(method = "handleMoveEntities", remap = false, require = 0)
    private void gc$watchMoves(RegistryFriendlyByteBuf buf, Operation<Void> original) {
        int start = buf.readerIndex();
        FlashbackChunks.scanMoves(this, buf);
        buf.readerIndex(start);
        original.call(buf);
    }
}

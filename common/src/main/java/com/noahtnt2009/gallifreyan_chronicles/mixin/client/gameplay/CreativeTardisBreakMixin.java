package com.noahtnt2009.gallifreyan_chronicles.mixin.client.gameplay;

import com.noahtnt2009.gallifreyan_chronicles.util.TardisDimensions;
import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.block.InteriorDoorBarrierBlock;
import com.noahtnt2009.gallifreyan_chronicles.block.InteriorDoorBlock;
import com.noahtnt2009.gallifreyan_chronicles.block.TardisBlock;
import com.noahtnt2009.gallifreyan_chronicles.block.TardisTopBlock;
import com.noahtnt2009.gallifreyan_chronicles.init.GCBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("unused")
@Mixin(MultiPlayerGameMode.class)
public abstract class CreativeTardisBreakMixin {
    @SuppressWarnings({"unused", "resource"})
    @Inject(method = "destroyBlock", at = @At("HEAD"), cancellable = true)
    private void gc$leaveTardisBreakToServer(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        boolean insideTardis = TardisDimensions.isInterior(mc.level);
        boolean remoteContext = !mc.player.level().dimension().equals(mc.level.dimension());
        if (insideTardis && !remoteContext) return;

        Block block = mc.level.getBlockState(pos).getBlock();
        boolean interiorDoor = block instanceof InteriorDoorBarrierBlock
                || block instanceof InteriorDoorBlock door && door.isLinked();
        boolean exterior = block instanceof TardisBlock
                || block instanceof TardisTopBlock && mc.level.getBlockState(pos.below()).is(GCBlocks.TARDIS);

        if (interiorDoor || exterior) cir.setReturnValue(false);
    }
}
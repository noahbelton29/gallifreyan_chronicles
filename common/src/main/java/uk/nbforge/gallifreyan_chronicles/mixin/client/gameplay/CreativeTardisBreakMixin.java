package uk.nbforge.gallifreyan_chronicles.mixin.client.gameplay;

import uk.nbforge.gallifreyan_chronicles.util.TardisDimensionUtil;
import uk.nbforge.gallifreyan_chronicles.core.block.InteriorDoorBarrierBlock;
import uk.nbforge.gallifreyan_chronicles.core.block.InteriorDoorBlock;
import uk.nbforge.gallifreyan_chronicles.core.block.TardisBlock;
import uk.nbforge.gallifreyan_chronicles.core.block.TardisTopBlock;
import uk.nbforge.gallifreyan_chronicles.core.GCBlocks;
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

        boolean insideTardis = TardisDimensionUtil.isInterior(mc.level);
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
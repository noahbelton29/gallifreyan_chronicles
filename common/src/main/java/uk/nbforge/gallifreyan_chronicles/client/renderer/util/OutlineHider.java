package uk.nbforge.gallifreyan_chronicles.client.renderer.util;

import uk.nbforge.gallifreyan_chronicles.core.block.TardisTopBlock;
import uk.nbforge.gallifreyan_chronicles.core.block.TardisBlock;
import uk.nbforge.gallifreyan_chronicles.core.block.InteriorDoorBarrierBlock;
import uk.nbforge.gallifreyan_chronicles.core.block.InteriorDoorBlock;
import uk.nbforge.gallifreyan_chronicles.compat.portal.TardisPortalBridge;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public final class OutlineHider {
    private OutlineHider() {
    }

    /* interior doors and the whole tardis exterior never show a block outline */
    private static boolean isDoorBlock(Block block) {
        return block instanceof InteriorDoorBlock || block instanceof InteriorDoorBarrierBlock
                || block instanceof TardisBlock || block instanceof TardisTopBlock;
    }

    public static boolean shouldHide(Level level, BlockPos pos) {
        return shouldHideInner(level, pos);
    }

    private static boolean shouldHideInner(Level level, BlockPos pos) {
        if (level != null && isDoorBlock(level.getBlockState(pos).getBlock())) return true;

        return TardisPortalBridge.isInteriorDoorAtInAnyTardisLevel(pos);
    }

    public static boolean shouldHide(BlockPos pos) {
        Minecraft mc = Minecraft.getInstance();
        return shouldHide(mc.level, pos);
    }
}
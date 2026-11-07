package com.noahtnt2009.gallifreyan_chronicles.client.renderer;

import com.noahtnt2009.gallifreyan_chronicles.block.InteriorDoorBarrierBlock;
import com.noahtnt2009.gallifreyan_chronicles.block.InteriorDoorBlock;
import com.noahtnt2009.gallifreyan_chronicles.compat.portal.TardisPortalBridge;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public final class OutlineHider {
    private OutlineHider() {
    }

    private static boolean isDoorBlock(Block block) {
        return block instanceof InteriorDoorBlock || block instanceof InteriorDoorBarrierBlock;
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
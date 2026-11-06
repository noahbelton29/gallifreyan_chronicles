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

    public static boolean shouldHide(Level level, BlockPos pos) {
        Block block = level.getBlockState(pos).getBlock();
        if (block instanceof InteriorDoorBlock || block instanceof InteriorDoorBarrierBlock) return true;
        return TardisPortalBridge.isRenderingTardisPortal();
    }

    public static boolean shouldHide(BlockPos pos) {
        Minecraft mc = Minecraft.getInstance();
        return mc.level != null && shouldHide(mc.level, pos);
    }
}
package com.noahtnt2009.gallifreyan_chronicles.client.renderer;

import com.noahtnt2009.gallifreyan_chronicles.block.InteriorDoorBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

public final class OutlineHider {
    private OutlineHider() {
    }

    public static boolean shouldHide(BlockPos pos) {
        Minecraft mc = Minecraft.getInstance();
        return mc.level != null && mc.level.getBlockState(pos).getBlock() instanceof InteriorDoorBlock;
    }
}
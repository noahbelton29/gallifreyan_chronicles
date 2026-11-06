package com.noahtnt2009.gallifreyan_chronicles.client.renderer;

import com.noahtnt2009.gallifreyan_chronicles.block.InteriorDoorBarrierBlock;
import com.noahtnt2009.gallifreyan_chronicles.block.InteriorDoorBlock;
import com.noahtnt2009.gallifreyan_chronicles.block.TardisBlock;
import com.noahtnt2009.gallifreyan_chronicles.block.TardisTopBlock;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisExteriorBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.compat.portal.TardisPortalBridge;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

public final class OutlineHider {

    private static final boolean ALWAYS_HIDE_TARDIS_EXTERIOR = true;

    private OutlineHider() {
    }

    private static boolean isDoorBlock(Block block) {
        return block instanceof InteriorDoorBlock || block instanceof InteriorDoorBarrierBlock;
    }


    private static boolean isOpenTardisExterior(Level level, BlockPos pos) {
        Block block = level.getBlockState(pos).getBlock();
        if (!(block instanceof TardisBlock) && !(block instanceof TardisTopBlock)) return false;
        if (ALWAYS_HIDE_TARDIS_EXTERIOR) return true;
        BlockPos base = TardisTopBlock.baseOf(level, pos);
        return level.getBlockEntity(base) instanceof TardisExteriorBlockEntity tardis
                && !tardis.areDoorsFullyClosed();
    }

    public static boolean shouldHide(Level level, BlockPos pos) {
        if (level != null) {

            if (isDoorBlock(level.getBlockState(pos).getBlock())) return true;

            if (isOpenTardisExterior(level, pos)) return true;
        }

        if (TardisPortalBridge.isRenderingTardisPortal()) return true;


        if (TardisPortalBridge.isNearTardisPortal(3.0)) return true;


        if (TardisPortalBridge.isAvailable() && isBeyondReach(pos)) return true;


        if (TardisPortalBridge.isAvailable() && level != null && level.getBlockState(pos).isAir()) return true;


        return TardisPortalBridge.isInteriorDoorAtInAnyTardisLevel(pos);
    }

    private static boolean isBeyondReach(BlockPos pos) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return false;
        double reach = mc.player.blockInteractionRange() + 2.0;
        return mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(pos)) > reach * reach;
    }

    public static boolean shouldHide(BlockPos pos) {
        Minecraft mc = Minecraft.getInstance();
        return shouldHide(mc.level, pos);
    }
}
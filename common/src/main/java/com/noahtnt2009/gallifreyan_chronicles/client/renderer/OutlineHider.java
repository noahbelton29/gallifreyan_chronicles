package com.noahtnt2009.gallifreyan_chronicles.client.renderer;

import com.noahtnt2009.gallifreyan_chronicles.block.InteriorDoorBarrierBlock;
import com.noahtnt2009.gallifreyan_chronicles.block.InteriorDoorBlock;
import com.noahtnt2009.gallifreyan_chronicles.block.TardisBlock;
import com.noahtnt2009.gallifreyan_chronicles.block.TardisTopBlock;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisExteriorBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.compat.portal.TardisPortalBridge;
import com.noahtnt2009.gallifreyan_chronicles.init.GCBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

public final class OutlineHider {
    private OutlineHider() {
    }

    private static boolean isDoorBlock(Block block) {
        return block instanceof InteriorDoorBlock || block instanceof InteriorDoorBarrierBlock;
    }


    private static boolean isOpenTardisExterior(Level level, BlockPos pos) {
        Block block = level.getBlockState(pos).getBlock();

        if (!(block instanceof TardisBlock) && !(block instanceof TardisTopBlock)) {
            return false;
        }

        BlockPos base = TardisTopBlock.baseOf(level, pos);

        if (!(level.getBlockEntity(base) instanceof TardisExteriorBlockEntity tardis)) {
            return false;
        }

        return !tardis.areDoorsFullyClosed();
    }

    private static long lastLogMs;

    private static void debugLog(Level level, BlockPos pos, boolean result) {
        long now = System.currentTimeMillis();
        if (now - lastLogMs < 500) return;
        lastLogMs = now;
        Minecraft mc = Minecraft.getInstance();
        com.noahtnt2009.gallifreyan_chronicles.Constants.LOG.info(
                "[OutlineHider] level={} mc.level={} pos={} block={} hide={}",
                level == null ? "null" : level.dimension().identifier(),
                mc.level == null ? "null" : mc.level.dimension().identifier(),
                pos,
                level == null ? "null" : level.getBlockState(pos).getBlock(),
                result);
    }

    public static boolean shouldHide(Level level, BlockPos pos) {
        boolean result = shouldHideInner(level, pos);
        debugLog(level, pos, result);
        return result;
    }

    private static boolean shouldHideInner(Level level, BlockPos pos) {
        if (level != null) {

            if (isDoorBlock(level.getBlockState(pos).getBlock())) return true;

            if (isOpenTardisExterior(level, pos)) return true;
        }

        if (TardisPortalBridge.isRenderingTardisPortal()) return true;


        if (TardisPortalBridge.isNearTardisPortal(3.0)) return true;


        if (TardisPortalBridge.isAvailable() && isBeyondReach(pos)) return true;


        if (TardisPortalBridge.isAvailable() && level != null && level.getBlockState(pos).isAir()) return true;


        if (TardisPortalBridge.isInteriorDoorAtInAnyTardisLevel(pos)) return true;

        // The door may have been replaced by another block; its old footprint must stay outline-free.
        return TardisPortalBridge.isInDoorFootprintOfAnyTardisPortal(pos);
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
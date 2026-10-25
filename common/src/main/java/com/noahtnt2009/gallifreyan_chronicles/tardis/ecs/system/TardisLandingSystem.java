package com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.system;

import com.noahtnt2009.gallifreyan_chronicles.block.TardisBlock;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisExteriorBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.init.GCBlocks;
import com.noahtnt2009.gallifreyan_chronicles.tardis.flight.LandingTarget;
import com.noahtnt2009.gallifreyan_chronicles.tardis.manager.TardisManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;

import java.util.UUID;

public final class TardisLandingSystem {
    private TardisLandingSystem() {
    }

    public static boolean relocate(MinecraftServer server, UUID tardisId) {
        TardisManager manager = TardisManager.get(server);
        LandingTarget target = manager.getLandingTarget(tardisId);
        if (target == null) return false;

        ServerLevel oldLevel = server.getLevel(manager.getExteriorDimension(tardisId));
        BlockPos oldPos = manager.resolveExteriorBlockPos(tardisId);
        ServerLevel newLevel = server.getLevel(target.dimension());
        if (oldLevel == null || oldPos == null || newLevel == null
                || !(oldLevel.getBlockEntity(oldPos) instanceof TardisExteriorBlockEntity old)) {
            return false;
        }

        BlockPos pos = target.pos();
        if (newLevel.isOutsideBuildHeight(pos) || newLevel.isOutsideBuildHeight(pos.above())) return false;
        newLevel.getChunkAt(pos);

        if (newLevel == oldLevel && pos.equals(oldPos)) {
            old.setYaw(target.yaw());
            manager.setLandingTarget(tardisId, null);
            return true;
        }

        // the exterior is two blocks tall
        if (!newLevel.getBlockState(pos).canBeReplaced() || !newLevel.getBlockState(pos.above()).canBeReplaced()) {
            return false;
        }

        boolean glowing = old.getBlockState().getValue(TardisBlock.GLOWING);

        oldLevel.removeBlock(oldPos, false);
        if (oldLevel.getBlockState(oldPos.above()).is(GCBlocks.TARDIS_TOP)) {
            oldLevel.removeBlock(oldPos.above(), false);
        }

        newLevel.setBlock(
                pos,
                GCBlocks.TARDIS.defaultBlockState().setValue(TardisBlock.GLOWING, glowing),
                Block.UPDATE_ALL
        );
        if (!(newLevel.getBlockEntity(pos) instanceof TardisExteriorBlockEntity fresh)) return false;

        fresh.copyStateFrom(old);
        fresh.setYaw(target.yaw());
        fresh.spawnKeyEntity();

        manager.setRegisteredBlockPos(tardisId, pos);
        manager.setExteriorDimension(tardisId, newLevel.dimension());
        if (manager.getExteriorBlockPos(tardisId) != null) {
            manager.setExteriorBlockPos(tardisId, pos);
        }
        manager.setLandingTarget(tardisId, null);
        return true;
    }
}

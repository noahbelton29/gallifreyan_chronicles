package com.noahtnt2009.gallifreyan_chronicles.client.util;

import com.noahtnt2009.gallifreyan_chronicles.client.renderer.RenderLevelGuard;
import com.noahtnt2009.gallifreyan_chronicles.config.GCConfig;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

public final class ModelRenderRange {
    private ModelRenderRange() {
    }

    public static boolean inRangeAndLocal(BlockEntity blockEntity, Vec3 cameraPos) {
        return !RenderLevelGuard.isForeign(blockEntity.getLevel()) && inRange(blockEntity, cameraPos);
    }

    public static boolean inRange(BlockEntity blockEntity, Vec3 cameraPos) {
        double range = GCConfig.modelRenderDistance();
        return Vec3.atCenterOf(blockEntity.getBlockPos()).distanceToSqr(cameraPos) < range * range;
    }
}

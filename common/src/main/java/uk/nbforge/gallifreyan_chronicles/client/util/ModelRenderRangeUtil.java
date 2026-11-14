package uk.nbforge.gallifreyan_chronicles.client.util;

import uk.nbforge.gallifreyan_chronicles.client.renderer.util.RenderLevelGuard;
import uk.nbforge.gallifreyan_chronicles.config.GCConfig;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

public final class ModelRenderRangeUtil {
    private ModelRenderRangeUtil() {
    }

    public static boolean inRangeAndLocal(BlockEntity blockEntity, Vec3 cameraPos) {
        return !RenderLevelGuard.isForeign(blockEntity.getLevel()) && inRange(blockEntity, cameraPos);
    }

    public static boolean inRange(BlockEntity blockEntity, Vec3 cameraPos) {
        double range = GCConfig.modelRenderDistance();
        return Vec3.atCenterOf(blockEntity.getBlockPos()).distanceToSqr(cameraPos) < range * range;
    }
}

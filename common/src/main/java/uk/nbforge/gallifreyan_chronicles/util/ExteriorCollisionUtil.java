package uk.nbforge.gallifreyan_chronicles.util;

import uk.nbforge.gallifreyan_chronicles.core.block_entities.TardisExteriorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class ExteriorCollisionUtil {
    private ExteriorCollisionUtil() {
    }

    public static VoxelShape shape(BlockGetter level, BlockPos exteriorPos) {
        if (level.getBlockEntity(exteriorPos) instanceof TardisExteriorBlockEntity tardis
                && (tardis.isFullyInvisible() || !tardis.getDoorState().isClosed())) {
            return Shapes.empty();
        }
        return Shapes.block();
    }
}

package com.noahtnt2009.gallifreyan_chronicles.util;

import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisExteriorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class ExteriorCollision {
    private ExteriorCollision() {
    }

    public static VoxelShape shape(BlockGetter level, BlockPos exteriorPos) {
        if (level.getBlockEntity(exteriorPos) instanceof TardisExteriorBlockEntity tardis
                && (tardis.isFullyInvisible() || !tardis.getDoorState().isClosed())) {
            return Shapes.empty();
        }
        return Shapes.block();
    }
}

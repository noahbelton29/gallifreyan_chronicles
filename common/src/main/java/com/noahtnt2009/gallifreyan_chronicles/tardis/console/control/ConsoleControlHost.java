package com.noahtnt2009.gallifreyan_chronicles.tardis.console.control;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

public interface ConsoleControlHost {
    Level getHostLevel();
    BlockPos getHostPos();
    void markHostChanged();

    /** Horizontal facing of the host; control offsets are authored for {@link Direction#NORTH}. */
    default Direction getHostFacing() {
        return Direction.NORTH;
    }
}

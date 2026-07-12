package com.noahtnt2009.gallifreyan_chronicles.tardis.console.control;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface ConsoleControlHost {
    Level getHostLevel();
    BlockPos getHostPos();
    void markHostChanged();
}

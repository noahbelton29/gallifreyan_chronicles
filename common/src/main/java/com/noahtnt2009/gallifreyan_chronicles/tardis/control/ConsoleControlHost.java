package com.noahtnt2009.gallifreyan_chronicles.tardis.control;

import com.noahtnt2009.gallifreyan_chronicles.tardis.control.TardisControlRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public interface ConsoleControlHost {
    Level getHostLevel();
    BlockPos getHostPos();
    void markHostChanged();

    default Direction getHostFacing() {
        return Direction.NORTH;
    }

    default @Nullable String resolveControlAnimation(String controlId, boolean activated) {
        return TardisControlRegistry.get(controlId).getAnimation(activated);
    }

    default boolean controlSnapsWhenInactive(String controlId) {
        return TardisControlRegistry.get(controlId).snapsWhenInactive();
    }

    default boolean isHostUpsideDown() {
        return false;
    }
}

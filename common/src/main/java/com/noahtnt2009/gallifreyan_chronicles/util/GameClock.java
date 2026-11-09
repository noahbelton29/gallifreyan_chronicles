package com.noahtnt2009.gallifreyan_chronicles.util;

import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

import java.util.function.LongSupplier;

/* one game clock for timed effects (tardis fades, cloak).
   the server's game time is the same in every world, but on the client only the world
   you're standing in keeps its time up to date. a world you only see through a portal,
   or left a while ago, has an old time, so fades worked out against it were way off */
public final class GameClock {
    private static volatile @Nullable LongSupplier client;

    private GameClock() {
    }

    /* set once by the client: the time of the world the player is in */
    public static void setClient(LongSupplier supplier) {
        client = supplier;
    }

    public static long now(Level level) {
        LongSupplier supplier = client;
        if (level.isClientSide() && supplier != null) {
            long time = supplier.getAsLong();
            if (time >= 0L) return time;
        }
        return level.getGameTime();
    }
}

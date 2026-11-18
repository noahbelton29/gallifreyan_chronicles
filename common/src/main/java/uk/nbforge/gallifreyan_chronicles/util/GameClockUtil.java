package uk.nbforge.gallifreyan_chronicles.util;

import java.util.function.LongSupplier;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * The game time of the server is the same in every world but on the client only the world you stand in keeps its time up to date.
 * A world you only see through a portal or left a while ago has an old time so fades worked out against it were way off.
 */
public final class GameClockUtil {
    private static volatile @Nullable LongSupplier client;

    private GameClockUtil() {}

    public static void setClient(@Nullable LongSupplier supplier) {
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

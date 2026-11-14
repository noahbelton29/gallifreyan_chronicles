package uk.nbforge.gallifreyan_chronicles.util;

import uk.nbforge.gallifreyan_chronicles.Constants;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public final class TardisDimensionUtil {
    public static final String INTERIOR_PREFIX = "tardis/";

    private TardisDimensionUtil() {
    }

    public static boolean isInterior(@Nullable Identifier dimension) {
        return dimension != null
                && dimension.getNamespace().equals(Constants.MOD_ID)
                && dimension.getPath().startsWith(INTERIOR_PREFIX);
    }

    public static boolean isInterior(@Nullable ResourceKey<Level> dimension) {
        return dimension != null && isInterior(dimension.identifier());
    }

    public static boolean isInterior(@Nullable Level level) {
        return level != null && isInterior(level.dimension());
    }
}

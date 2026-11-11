package com.noahtnt2009.gallifreyan_chronicles.util;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public final class TardisDimensions {
    public static final String INTERIOR_PREFIX = "tardis/";

    private TardisDimensions() {
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

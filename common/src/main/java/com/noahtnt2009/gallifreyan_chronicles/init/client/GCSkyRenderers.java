package com.noahtnt2009.gallifreyan_chronicles.init.client;

import com.noahtnt2009.gallifreyan_chronicles.client.sky.DimensionSkyRenderer;
import com.noahtnt2009.gallifreyan_chronicles.client.sky.GallifreySkyRenderer;
import com.noahtnt2009.gallifreyan_chronicles.client.sky.MoonSkyRenderer;
import com.noahtnt2009.gallifreyan_chronicles.init.GCDimensions;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class GCSkyRenderers {
    private static final Map<ResourceKey<Level>, DimensionSkyRenderer> REGISTRY = new HashMap<>();

    public static void register(ResourceKey<Level> dimension, DimensionSkyRenderer renderer) {
        REGISTRY.put(dimension, renderer);
    }

    public static Optional<DimensionSkyRenderer> get(ResourceKey<Level> dimension) {
        return Optional.ofNullable(REGISTRY.get(dimension));
    }

    public static void registerSkyRenderers() {
        register(GCDimensions.GALLIFREY_LEVEL_KEY, new GallifreySkyRenderer());
        register(GCDimensions.MOON_LEVEL_KEY, new MoonSkyRenderer());
    }
}
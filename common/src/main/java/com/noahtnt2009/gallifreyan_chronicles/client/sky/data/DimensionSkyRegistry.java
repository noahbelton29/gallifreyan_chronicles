package com.noahtnt2009.gallifreyan_chronicles.client.sky.data;

import net.minecraft.resources.Identifier;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DimensionSkyRegistry {
    private static final Map<Identifier, DimensionSky> BY_DIMENSION = new HashMap<>();
    private static final Map<String, DimensionSky> BY_ID = new HashMap<>();

    public static void clear() {
        BY_DIMENSION.clear();
        BY_ID.clear();
    }

    public static DimensionSky register(DimensionSky sky) {
        BY_ID.put(sky.id(), sky);
        BY_DIMENSION.put(sky.dimension(), sky);
        return sky;
    }

    public static DimensionSky getForDimension(Identifier dimension) {
        return BY_DIMENSION.get(dimension);
    }

    public static DimensionSky getById(String id) {
        return BY_ID.get(id);
    }

    public static boolean hasDimension(Identifier dimension) {
        return BY_DIMENSION.containsKey(dimension);
    }

    public static Collection<DimensionSky> getAll() {
        return Collections.unmodifiableCollection(BY_ID.values());
    }

    public static int size() {
        return BY_ID.size();
    }
}

package com.noahtnt2009.gallifreyan_chronicles.client.sky.data;

import com.noahtnt2009.gallifreyan_chronicles.data.GCJsonDataRegistry;
import net.minecraft.resources.Identifier;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


public class DimensionSkyRegistry {
    private static final GCJsonDataRegistry<DimensionSky> BY_ID = new GCJsonDataRegistry<>();
    private static final Map<Identifier, DimensionSky> BY_DIMENSION = new HashMap<>();

    private DimensionSkyRegistry() {}

    public static void clear() {
        BY_ID.clear();
        BY_DIMENSION.clear();
    }

    public static DimensionSky register(DimensionSky sky) {
        BY_DIMENSION.put(sky.dimension(), sky);
        return BY_ID.register(sky);
    }

    public static DimensionSky getForDimension(Identifier dimension) {
        return BY_DIMENSION.get(dimension);
    }

    public static DimensionSky getById(String id) {
        return BY_ID.getById(id);
    }

    public static boolean hasDimension(Identifier dimension) {
        return BY_DIMENSION.containsKey(dimension);
    }

    public static Collection<DimensionSky> getAll() {
        return BY_ID.getAll();
    }

    public static int size() {
        return BY_ID.size();
    }
}

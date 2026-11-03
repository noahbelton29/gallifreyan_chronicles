package com.noahtnt2009.gallifreyan_chronicles.sky;

import com.noahtnt2009.gallifreyan_chronicles.data.GCJsonDataRegistry;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.Collection;

public final class CelestialBodyRegistry {
    private static final GCJsonDataRegistry<CelestialBody> BY_ID = new GCJsonDataRegistry<>();

    private CelestialBodyRegistry() {
    }

    public static void clear() {
        BY_ID.clear();
    }

    public static CelestialBody register(CelestialBody body) {
        return BY_ID.register(body);
    }

    public static @Nullable CelestialBody get(Identifier id) {
        return BY_ID.getById(id.toString());
    }

    public static Collection<CelestialBody> getAll() {
        return BY_ID.getAll();
    }

    public static int size() {
        return BY_ID.size();
    }
}

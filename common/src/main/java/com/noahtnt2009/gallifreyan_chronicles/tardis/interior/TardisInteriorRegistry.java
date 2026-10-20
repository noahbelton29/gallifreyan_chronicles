package com.noahtnt2009.gallifreyan_chronicles.tardis.interior;

import com.noahtnt2009.gallifreyan_chronicles.config.GCConfig;
import com.noahtnt2009.gallifreyan_chronicles.data.GCJsonDataRegistry;

import java.util.Collection;

public final class TardisInteriorRegistry {
    public static String defaultId() {
        return GCConfig.defaultInterior();
    }

    private static final GCJsonDataRegistry<TardisInterior> REGISTRY = new GCJsonDataRegistry<>();

    private TardisInteriorRegistry() {
    }

    public static void clear() {
        REGISTRY.clear();
    }

    public static TardisInterior register(TardisInterior interior) {
        return REGISTRY.register(interior);
    }

    public static boolean contains(String id) {
        return REGISTRY.contains(id);
    }

    public static Collection<TardisInterior> getAll() {
        return REGISTRY.getAll();
    }

    public static int size() {
        return REGISTRY.size();
    }

    public static TardisInterior get(String id) {
        TardisInterior found = id == null ? null : REGISTRY.getById(id);
        if (found != null) return found;
        found = REGISTRY.getById(defaultId());
        if (found != null) return found;
        return REGISTRY.getAll().stream().findFirst().orElse(TardisInterior.FALLBACK);
    }
}

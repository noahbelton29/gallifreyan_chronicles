package com.noahtnt2009.gallifreyan_chronicles.tardis.interior;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.data.GCJsonDataRegistry;

import java.util.Collection;

public final class TardisInteriorRegistry {
    public static final String DEFAULT_ID = Constants.MOD_ID + ":first_doctors_interior";

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

    /** Looks up by id, falling back to the default interior, then any interior, then the built-in fallback. */
    public static TardisInterior get(String id) {
        TardisInterior found = id == null ? null : REGISTRY.getById(id);
        if (found != null) return found;
        found = REGISTRY.getById(DEFAULT_ID);
        if (found != null) return found;
        return REGISTRY.getAll().stream().findFirst().orElse(TardisInterior.FALLBACK);
    }

    /** The interior that lists the given exterior, otherwise the default. */
    public static TardisInterior forExterior(String exteriorId) {
        for (TardisInterior interior : REGISTRY.getAll()) {
            if (interior.exteriors().contains(exteriorId)) return interior;
        }
        return get(DEFAULT_ID);
    }
}

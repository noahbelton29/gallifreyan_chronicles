package com.noahtnt2009.gallifreyan_chronicles.tardis.exterior;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class TardisExteriorRegistry {

    private static final Map<String, TardisExterior> REGISTRY = new HashMap<>();
    private static TardisExterior defaultExterior;

    public static TardisExterior register(TardisExterior exterior) {
        REGISTRY.put(exterior.id(), exterior);
        return exterior;
    }

    public static TardisExterior get(String id) {
        return REGISTRY.getOrDefault(id, defaultExterior);
    }
    public static boolean contains(String id) {
        return REGISTRY.containsKey(id);
    }

    public static Collection<TardisExterior> getAll() {
        return REGISTRY.values();
    }


    public static void setDefault(TardisExterior exterior) {
        defaultExterior = exterior;
    }

    public static TardisExterior getDefault() {
        return defaultExterior;
    }
}
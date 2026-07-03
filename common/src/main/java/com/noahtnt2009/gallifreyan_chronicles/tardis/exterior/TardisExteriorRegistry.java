package com.noahtnt2009.gallifreyan_chronicles.tardis.exterior;

import com.noahtnt2009.gallifreyan_chronicles.Constants;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TardisExteriorRegistry {
    public static final String DEFAULT_ID =
            Constants.MOD_ID + ":first_doctors_exterior";

    private static final Map<String, TardisExterior> REGISTRY = new HashMap<>();
    private static TardisExterior defaultExterior;

    public static void clear() {
        REGISTRY.clear();
        defaultExterior = null;
    }

    public static TardisExterior register(TardisExterior exterior) {
        REGISTRY.put(exterior.id(), exterior);
        return exterior;
    }

    public static void setDefault(TardisExterior exterior) {
        defaultExterior = exterior;
    }

    public static TardisExterior get(String id) {
        TardisExterior result = REGISTRY.get(id);
        if (result != null) return result;
        if (defaultExterior != null) return defaultExterior;
        return REGISTRY.values().iterator().next();
    }

    public static boolean contains(String id) {
        return REGISTRY.containsKey(id);
    }

    public static Collection<TardisExterior> getAll() {
        return Collections.unmodifiableCollection(REGISTRY.values());
    }

    public static TardisExterior getDefault() {
        return defaultExterior;
    }

    public static int size() {
        return REGISTRY.size();
    }
}

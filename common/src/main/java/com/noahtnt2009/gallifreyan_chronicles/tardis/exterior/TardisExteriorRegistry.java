package com.noahtnt2009.gallifreyan_chronicles.tardis.exterior;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.data.GCJsonDataRegistry;

import java.util.Collection;

public class TardisExteriorRegistry {
    public static final String DEFAULT_ID =
            Constants.MOD_ID + ":first_doctors_exterior";

    private static final GCJsonDataRegistry<TardisExterior> REGISTRY = new GCJsonDataRegistry<>();
    private static TardisExterior defaultExterior;

    private TardisExteriorRegistry() {
    }

    public static void clear() {
        REGISTRY.clear();
        defaultExterior = null;
    }

    public static TardisExterior register(TardisExterior exterior) {
        return REGISTRY.register(exterior);
    }

    public static void setDefault(TardisExterior exterior) {
        defaultExterior = exterior;
    }

    public static TardisExterior get(String id) {
        TardisExterior result = REGISTRY.getById(id);
        if (result != null) return result;
        if (defaultExterior != null) return defaultExterior;
        return REGISTRY.getAll().iterator().next();
    }

    public static boolean contains(String id) {
        return REGISTRY.contains(id);
    }
    public static Collection<TardisExterior> getAll() {
        return REGISTRY.getAll();
    }
    public static TardisExterior getDefault() {
        return defaultExterior;
    }
    public static int size() {
        return REGISTRY.size();
    }
}

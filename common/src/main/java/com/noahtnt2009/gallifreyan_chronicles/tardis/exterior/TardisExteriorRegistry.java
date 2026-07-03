package com.noahtnt2009.gallifreyan_chronicles.tardis.exterior;

import com.noahtnt2009.gallifreyan_chronicles.Constants;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Runtime registry for all loaded {@link TardisExterior} definitions.
 * Populated by {@link TardisExteriorLoader} on every resource reload.
 */
public class TardisExteriorRegistry {

    /** The id used as the fallback when an unknown exterior is requested. */
    public static final String DEFAULT_ID =
            Constants.MOD_ID + ":first_doctors_exterior";

    private static final Map<String, TardisExterior> REGISTRY = new HashMap<>();
    private static TardisExterior defaultExterior;

    // -------------------------------------------------------------------------
    // Mutation (called by loader)
    // -------------------------------------------------------------------------

    /** Remove all registered exteriors. Called at the start of each reload. */
    public static void clear() {
        REGISTRY.clear();
        defaultExterior = null;
    }

    /** Register an exterior. Overwrites any previous entry with the same id. */
    public static TardisExterior register(TardisExterior exterior) {
        REGISTRY.put(exterior.id(), exterior);
        return exterior;
    }

    public static void setDefault(TardisExterior exterior) {
        defaultExterior = exterior;
    }

    // -------------------------------------------------------------------------
    // Query
    // -------------------------------------------------------------------------

    /**
     * Return the exterior for {@code id}, or the default exterior if not found.
     * Never returns {@code null} as long as at least one exterior was loaded.
     */
    public static TardisExterior get(String id) {
        TardisExterior result = REGISTRY.get(id);
        if (result != null) return result;
        if (defaultExterior != null) return defaultExterior;
        // Last resort: first registered entry
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

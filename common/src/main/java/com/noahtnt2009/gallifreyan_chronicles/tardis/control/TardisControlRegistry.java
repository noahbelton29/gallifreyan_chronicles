package com.noahtnt2009.gallifreyan_chronicles.tardis.control;

import com.noahtnt2009.gallifreyan_chronicles.Constants;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class TardisControlRegistry {
    // insertion ordered so iteration (datagen, console setup, suggestions) is stable
    private static final Map<String, TardisControl> CONTROLS = new LinkedHashMap<>();

    public static void register(String id, TardisControl control) {
        if (CONTROLS.putIfAbsent(id, control) != null) {
            throw new IllegalArgumentException("Duplicate TARDIS control: " + id);
        }
    }

    public static TardisControl get(String id) {
        return CONTROLS.getOrDefault(id, NoopControl.INSTANCE);
    }

    public static Set<String> allIds() {
        return Collections.unmodifiableSet(CONTROLS.keySet());
    }

    public static Map<String, TardisControl> all() {
        return Collections.unmodifiableMap(CONTROLS);
    }

    /** The lang key for a control's display name, used at runtime and by the language provider. */
    public static String translationKey(String id) {
        return "control." + Constants.MOD_ID + "." + id;
    }
}

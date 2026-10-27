package com.noahtnt2009.gallifreyan_chronicles.tardis.control;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TardisControlRegistry {
    private static final Map<String, TardisControl> CONTROLS = new HashMap<>();

    public static void register(String id, TardisControl control) {
        CONTROLS.put(id, control);
    }

    public static TardisControl get(String id) {
        return CONTROLS.getOrDefault(id, NoopControl.INSTANCE);
    }

    public static Set<String> allIds() {
        return Collections.unmodifiableSet(CONTROLS.keySet());
    }
}
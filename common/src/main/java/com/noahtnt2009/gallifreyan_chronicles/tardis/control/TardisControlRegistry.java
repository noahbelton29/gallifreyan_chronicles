package com.noahtnt2009.gallifreyan_chronicles.tardis.control;

import com.noahtnt2009.gallifreyan_chronicles.tardis.control.impl.FlightLeverControl;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.impl.HandbrakeControl;

import java.util.HashMap;
import java.util.Map;

public class TardisControlRegistry {
    private static final Map<String, TardisControl> CONTROLS = new HashMap<>();
    
    static {
        register("flight_lever", new FlightLeverControl());
        register("handbrake", new HandbrakeControl());
    }

    public static void register(String id, TardisControl control) {
        CONTROLS.put(id, control);
    }

    public static TardisControl get(String id) {
        return CONTROLS.getOrDefault(id, new TardisControl() {});
    }

    public static java.util.Set<String> allIds() {
        return java.util.Collections.unmodifiableSet(CONTROLS.keySet());
    }
}
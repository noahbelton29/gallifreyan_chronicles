package com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.component;

import java.util.Arrays;
import java.util.Optional;

/**
 * A damageable part of the TARDIS. Add new subsystems here; they get 100 health by default
 * and are saved/loaded automatically by {@link SubsystemsComponent}.
 */
public enum TardisSubsystem {
    DOOR_LOCK("door_lock");

    private final String id;

    TardisSubsystem(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public static Optional<TardisSubsystem> byId(String id) {
        return Arrays.stream(values()).filter(s -> s.id.equals(id)).findFirst();
    }
}

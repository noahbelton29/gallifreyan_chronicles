package com.noahtnt2009.gallifreyan_chronicles.tardis.flight;

// where a tardis is in its trip, TAKING_OFF and LANDING are timed and move on by themselves, IN_VORTEX lasts until the
// flight lever goes back up
public enum TardisFlightState {
    IDLE,
    TAKING_OFF,
    IN_VORTEX,
    LANDING;

    public boolean isIdle() {
        return this == IDLE;
    }

    // the lever can't be used and the doors stay shut during these, the engines are still spinning up or winding down
    public boolean isTransitioning() {
        return this == TAKING_OFF || this == LANDING;
    }

    public boolean isTimed() {
        return this == TAKING_OFF || this == LANDING;
    }

    // the state a timed state moves on to once its time is up
    public TardisFlightState afterTimer() {
        return switch (this) {
            case TAKING_OFF -> IN_VORTEX;
            case LANDING -> IDLE;
            default -> this;
        };
    }

    public static TardisFlightState parse(String name, TardisFlightState fallback) {
        for (TardisFlightState state : values()) {
            if (state.name().equals(name)) return state;
        }
        return fallback;
    }
}

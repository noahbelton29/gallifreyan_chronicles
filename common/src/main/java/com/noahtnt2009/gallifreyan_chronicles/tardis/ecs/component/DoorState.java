package com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.component;

public enum DoorState {
    CLOSED,
    RIGHT_OPEN,
    LEFT_OPEN,
    BOTH_OPEN;

    public boolean isLeftOpen() {
        return this == LEFT_OPEN || this == BOTH_OPEN;
    }

    public boolean isRightOpen() {
        return this == RIGHT_OPEN || this == BOTH_OPEN;
    }

    public boolean isBothOpen() {
        return this == BOTH_OPEN;
    }

    public boolean isClosed() {
        return this == CLOSED;
    }
}

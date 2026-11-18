package uk.nbforge.gallifreyan_chronicles.tardis.door;

import org.jspecify.annotations.Nullable;

public final class DoorMotion {
    private static final long OPEN_DURATION_MS = 600;
    private static final long CLOSE_DURATION_MS = 400;

    private DoorMotion() {}

    /** next is the new door status, null when the door was mid animation and nothing changed */
    public record InteractionResult(
            @Nullable String animationName,
            boolean opening,
            DoorState state,
            @Nullable DoorStatus next) {
        public boolean hasNoAnimation() {
            return animationName == null;
        }
    }

    public static DoorStatus applyRemoteDoorState(DoorStatus door, DoorState next, boolean opening, long now) {
        long duration = opening ? OPEN_DURATION_MS : CLOSE_DURATION_MS;
        return door.animatingUntil(next, now + duration);
    }

    public static DoorStatus snapToState(DoorStatus door, DoorState state) {
        return door.animatingUntil(state, 0L);
    }

    public static InteractionResult interact(DoorStatus door, boolean sneaking, long now) {
        return interact(door, sneaking, now, false);
    }

    /** openAllOnly is for doors with no left and right panels where every click just toggles closed and fully open */
    public static InteractionResult interact(DoorStatus door, boolean sneaking, long now, boolean openAllOnly) {
        if (door.isAnimating(now)) {
            return new InteractionResult(null, false, door.state(), null);
        }

        DoorState current = door.state();
        DoorState next;
        String animation;
        long duration;
        boolean opening;

        if (openAllOnly) {
            if (current == DoorState.CLOSED) {
                next = DoorState.BOTH_OPEN;
                animation = "open_all";
                duration = OPEN_DURATION_MS;
                opening = true;
            } else {
                next = DoorState.CLOSED;
                animation = "close_all";
                duration = CLOSE_DURATION_MS;
                opening = false;
            }
        } else if (sneaking) {
            switch (current) {
                case CLOSED -> {
                    next = DoorState.BOTH_OPEN;
                    animation = "open_all";
                    duration = OPEN_DURATION_MS;
                    opening = true;
                }
                case LEFT_OPEN -> {
                    next = DoorState.CLOSED;
                    animation = "close_left";
                    duration = CLOSE_DURATION_MS;
                    opening = false;
                }
                case RIGHT_OPEN -> {
                    next = DoorState.CLOSED;
                    animation = "close_right";
                    duration = CLOSE_DURATION_MS;
                    opening = false;
                }
                default -> {
                    next = DoorState.CLOSED;
                    animation = "close_all";
                    duration = CLOSE_DURATION_MS;
                    opening = false;
                }
            }
        } else {
            switch (current) {
                case CLOSED -> {
                    next = DoorState.RIGHT_OPEN;
                    animation = "open_right";
                    duration = OPEN_DURATION_MS;
                    opening = true;
                }
                case RIGHT_OPEN -> {
                    next = DoorState.BOTH_OPEN;
                    animation = "open_left";
                    duration = OPEN_DURATION_MS;
                    opening = true;
                }
                default -> {
                    next = DoorState.CLOSED;
                    animation = "close_all";
                    duration = CLOSE_DURATION_MS;
                    opening = false;
                }
            }
        }

        return new InteractionResult(animation, opening, next, door.animatingUntil(next, now + duration));
    }
}

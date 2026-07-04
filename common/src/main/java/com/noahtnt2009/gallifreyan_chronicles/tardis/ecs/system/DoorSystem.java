package com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.system;

import com.noahtnt2009.gallifreyan_chronicles.ecs.Entity;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.component.TardisComponentTypes;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.component.DoorComponent;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.component.DoorState;

public final class DoorSystem {
    private static final long OPEN_DURATION_MS = 600;
    private static final long CLOSE_DURATION_MS = 400;

    private DoorSystem() {
    }

    public record InteractionResult(String animationName, boolean opening) {
        public boolean playedNothing() {
            return animationName == null;
        }
    }

    public static DoorState stateOf(Entity entity) {
        return entity.get(TardisComponentTypes.DOOR).state();
    }

    public static boolean isAnimating(Entity entity, long now) {
        return entity.get(TardisComponentTypes.DOOR).isAnimating(now);
    }

    public static InteractionResult interact(Entity entity, boolean sneaking, long now) {
        DoorComponent door = entity.get(TardisComponentTypes.DOOR);
        if (door.isAnimating(now)) {
            return new InteractionResult(null, false);
        }

        DoorState current = door.state();
        DoorState next;
        String animation;
        long duration;
        boolean opening;

        if (sneaking) {
            if (current.isClosed()) {
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

        entity.set(TardisComponentTypes.DOOR, door.animatingUntil(next, now + duration));
        return new InteractionResult(animation, opening);
    }
}

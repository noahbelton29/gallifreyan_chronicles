package uk.nbforge.gallifreyan_chronicles.tardis.ecs.component;

import uk.nbforge.gallifreyan_chronicles.ecs.Component;

public record DoorComponent(DoorState state, long animationEndTime) implements Component {
    public static DoorComponent closed() {
        return new DoorComponent(DoorState.CLOSED, 0L);
    }

    public boolean isAnimating(long now) {
        return now < animationEndTime;
    }

    public DoorComponent withState(DoorState newState) {
        return new DoorComponent(newState, animationEndTime);
    }

    public DoorComponent animatingUntil(DoorState newState, long endTime) {
        return new DoorComponent(newState, endTime);
    }
}

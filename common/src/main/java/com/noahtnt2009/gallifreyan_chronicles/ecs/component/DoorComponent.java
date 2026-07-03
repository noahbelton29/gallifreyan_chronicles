package com.noahtnt2009.gallifreyan_chronicles.ecs.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.noahtnt2009.gallifreyan_chronicles.ecs.Component;

public record DoorComponent(DoorState state, long animationEndTime) implements Component {
    public static final Codec<DoorComponent> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.STRING.xmap(DoorState::valueOf, Enum::name).fieldOf("state").forGetter(DoorComponent::state)
    ).apply(inst, state -> new DoorComponent(state, 0L)));

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

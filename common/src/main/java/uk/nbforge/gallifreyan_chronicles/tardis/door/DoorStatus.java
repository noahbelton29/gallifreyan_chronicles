package uk.nbforge.gallifreyan_chronicles.tardis.door;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** only the state is saved, a loaded door is never mid animation */
public record DoorStatus(DoorState state, long animationEndTime) {
    public static final Codec<DoorStatus> CODEC = RecordCodecBuilder.create(inst -> inst.group(Codec.STRING
                    .xmap(DoorState::valueOf, Enum::name)
                    .fieldOf("state")
                    .forGetter(DoorStatus::state))
            .apply(inst, state -> new DoorStatus(state, 0L)));

    public static DoorStatus closed() {
        return new DoorStatus(DoorState.CLOSED, 0L);
    }

    public boolean isAnimating(long now) {
        return now < animationEndTime;
    }

    public DoorStatus animatingUntil(DoorState newState, long endTime) {
        return new DoorStatus(newState, endTime);
    }
}

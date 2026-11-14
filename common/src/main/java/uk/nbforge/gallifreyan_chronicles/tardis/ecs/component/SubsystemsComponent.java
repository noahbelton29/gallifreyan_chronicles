package uk.nbforge.gallifreyan_chronicles.tardis.ecs.component;

import uk.nbforge.gallifreyan_chronicles.ecs.Component;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public record SubsystemsComponent(Map<TardisSubsystem, HealthComponent> healths) implements Component {
    public SubsystemsComponent {
        Map<TardisSubsystem, HealthComponent> copy = new EnumMap<>(TardisSubsystem.class);
        copy.putAll(healths);
        healths = Collections.unmodifiableMap(copy);
    }

    public static SubsystemsComponent fresh() {
        return new SubsystemsComponent(Map.of());
    }

    public HealthComponent healthOf(TardisSubsystem subsystem) {
        return healths.getOrDefault(subsystem, HealthComponent.full());
    }

    public SubsystemsComponent withHealth(TardisSubsystem subsystem, HealthComponent health) {
        Map<TardisSubsystem, HealthComponent> next = new EnumMap<>(TardisSubsystem.class);
        next.putAll(healths);
        next.put(subsystem, health);
        return new SubsystemsComponent(next);
    }
}

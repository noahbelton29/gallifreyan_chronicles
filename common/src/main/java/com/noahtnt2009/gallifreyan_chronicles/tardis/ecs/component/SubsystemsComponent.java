package com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.component;

import com.mojang.serialization.Codec;
import com.noahtnt2009.gallifreyan_chronicles.ecs.Component;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public record SubsystemsComponent(Map<TardisSubsystem, HealthComponent> healths) implements Component {
    public static final Codec<SubsystemsComponent> CODEC =
            Codec.unboundedMap(Codec.STRING, HealthComponent.CODEC).xmap(
                    raw -> {
                        Map<TardisSubsystem, HealthComponent> parsed = new EnumMap<>(TardisSubsystem.class);
                        raw.forEach((key, health) -> TardisSubsystem.byId(key)
                                .ifPresent(subsystem -> parsed.put(subsystem, health)));
                        return new SubsystemsComponent(parsed);
                    },
                    component -> {
                        Map<String, HealthComponent> out = new HashMap<>();
                        component.healths.forEach((subsystem, health) -> out.put(subsystem.id(), health));
                        return out;
                    }
            );

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

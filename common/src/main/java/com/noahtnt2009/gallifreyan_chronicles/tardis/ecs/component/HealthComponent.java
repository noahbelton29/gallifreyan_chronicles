package com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.component;

import com.mojang.serialization.Codec;
import com.noahtnt2009.gallifreyan_chronicles.ecs.Component;

/**
 * Health of a damageable thing (currently a {@link TardisSubsystem}), clamped to 0 - 100.
 */
public record HealthComponent(float value) implements Component {
    public static final float MAX = 100f;

    // stored as a plain float so it stays compatible with saves from before this component existed
    public static final Codec<HealthComponent> CODEC =
            Codec.FLOAT.xmap(HealthComponent::new, HealthComponent::value);

    public HealthComponent {
        value = Math.max(0f, Math.min(MAX, value));
    }

    public static HealthComponent full() {
        return new HealthComponent(MAX);
    }

    public HealthComponent withValue(float newValue) {
        return new HealthComponent(newValue);
    }

    public HealthComponent damaged(float amount) {
        return new HealthComponent(value - amount);
    }

    public HealthComponent repaired(float amount) {
        return new HealthComponent(value + amount);
    }

    public boolean isDestroyed() {
        return value <= 0f;
    }
}

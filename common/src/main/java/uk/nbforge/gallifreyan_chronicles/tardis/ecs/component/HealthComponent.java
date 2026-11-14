package uk.nbforge.gallifreyan_chronicles.tardis.ecs.component;

import uk.nbforge.gallifreyan_chronicles.ecs.Component;

public record HealthComponent(float value) implements Component {
    public static final float MAX = 100f;

    public HealthComponent {
        value = Math.clamp(value, 0f, MAX);
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

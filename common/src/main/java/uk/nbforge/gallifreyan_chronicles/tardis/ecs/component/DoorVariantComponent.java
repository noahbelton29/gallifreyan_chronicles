package uk.nbforge.gallifreyan_chronicles.tardis.ecs.component;

import uk.nbforge.gallifreyan_chronicles.ecs.Component;

public record DoorVariantComponent(String variantId) implements Component {
    public static DoorVariantComponent unset() {
        return new DoorVariantComponent("");
    }

    public boolean isSet() {
        return !variantId.isEmpty();
    }
}

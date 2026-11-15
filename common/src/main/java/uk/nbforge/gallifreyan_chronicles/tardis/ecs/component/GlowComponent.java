package uk.nbforge.gallifreyan_chronicles.tardis.ecs.component;

import uk.nbforge.gallifreyan_chronicles.ecs.Component;

/** glowing is whether the lamp is lit right now, lampOn is whether it is switched on, on by default and it only lights with power. */
public record GlowComponent(boolean glowing, boolean lampOn) implements Component {
    public static GlowComponent fresh() {
        return new GlowComponent(false, true);
    }

    public GlowComponent withGlowing(boolean glowing) {
        return new GlowComponent(glowing, lampOn);
    }

    public GlowComponent withLampOn(boolean lampOn) {
        return new GlowComponent(glowing, lampOn);
    }
}

package uk.nbforge.gallifreyan_chronicles.tardis.ecs.system;

import uk.nbforge.gallifreyan_chronicles.ecs.Entity;
import uk.nbforge.gallifreyan_chronicles.tardis.ecs.component.GlowComponent;
import uk.nbforge.gallifreyan_chronicles.tardis.ecs.TardisComponentTypes;

/** The lamp is on by default and lights whenever there is power, a control can switch it off later. */
public final class GlowSystem {
    private GlowSystem() {
    }

    public static boolean glowingOf(Entity entity) {
        return entity.get(TardisComponentTypes.GLOW).glowing();
    }

    public static boolean lampOnOf(Entity entity) {
        return entity.get(TardisComponentTypes.GLOW).lampOn();
    }

    /** Flips the lamp switch, it only lights if there is power. */
    public static void setLamp(Entity entity, boolean on) {
        GlowComponent current = entity.get(TardisComponentTypes.GLOW);
        entity.set(TardisComponentTypes.GLOW, current.withLampOn(on).withGlowing(on));
    }

    /** Returns true if the lamp just turned on or off. */
    public static boolean tick(Entity entity, boolean powered) {
        GlowComponent glow = entity.get(TardisComponentTypes.GLOW);
        boolean shouldGlow = powered && glow.lampOn();
        if (glow.glowing() == shouldGlow) return false;
        entity.set(TardisComponentTypes.GLOW, glow.withGlowing(shouldGlow));
        return true;
    }
}

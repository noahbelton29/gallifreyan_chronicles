package com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.system;

import com.noahtnt2009.gallifreyan_chronicles.ecs.Entity;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.component.GlowComponent;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.TardisComponentTypes;

/* the lamp is on by default and lights whenever there's power. a control can switch it off later */
public final class GlowSystem {
    private GlowSystem() {
    }

    public static boolean glowingOf(Entity entity) {
        return entity.get(TardisComponentTypes.GLOW).glowing();
    }

    public static boolean lampOnOf(Entity entity) {
        return entity.get(TardisComponentTypes.GLOW).lampOn();
    }

    /* flips the lamp switch. it only actually lights if there's power */
    public static void setLamp(Entity entity, boolean on) {
        GlowComponent current = entity.get(TardisComponentTypes.GLOW);
        entity.set(TardisComponentTypes.GLOW, current.withLampOn(on).withGlowing(on));
    }

    /* returns true if the lamp just turned on or off */
    public static boolean tick(Entity entity, boolean powered) {
        GlowComponent glow = entity.get(TardisComponentTypes.GLOW);
        boolean shouldGlow = powered && glow.lampOn();
        if (glow.glowing() == shouldGlow) return false;
        entity.set(TardisComponentTypes.GLOW, glow.withGlowing(shouldGlow));
        return true;
    }
}

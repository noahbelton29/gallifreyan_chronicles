package com.noahtnt2009.gallifreyan_chronicles.ecs.system;

import com.noahtnt2009.gallifreyan_chronicles.ecs.Entity;
import com.noahtnt2009.gallifreyan_chronicles.ecs.component.ComponentTypes;
import com.noahtnt2009.gallifreyan_chronicles.ecs.component.GlowComponent;

public final class GlowSystem {
    private static final long NIGHT_START = 13000L;
    private static final long NIGHT_END = 23000L;

    private GlowSystem() {
    }

    public static boolean isNight(long dayTime) {
        long time = dayTime % 24000L;
        return time >= NIGHT_START && time <= NIGHT_END;
    }

    public static boolean glowingOf(Entity entity) {
        return entity.get(ComponentTypes.GLOW).glowing();
    }

    public static void setManual(Entity entity, boolean glowing) {
        GlowComponent current = entity.get(ComponentTypes.GLOW);
        entity.set(ComponentTypes.GLOW, current.manualSet(glowing));
    }

    public static boolean tick(Entity entity, long dayTime, boolean daylightCycleAffectsGlow) {
        GlowComponent glow = entity.get(ComponentTypes.GLOW);

        if (!daylightCycleAffectsGlow) {
            return false;
        }

        boolean naturalState = isNight(dayTime);

        if (glow.manualOverride()) {
            if (glow.glowing() == naturalState) {
                entity.set(ComponentTypes.GLOW, glow.clearOverride());
                return false;
            }
            return false;
        }

        if (glow.glowing() == naturalState) {
            return false;
        }

        entity.set(ComponentTypes.GLOW, glow.withGlowing(naturalState));
        return true;
    }
}
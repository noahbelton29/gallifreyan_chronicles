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

    public static boolean tick(Entity entity, long dayTime) {
        boolean shouldGlow = isNight(dayTime);
        boolean current = glowingOf(entity);

        if (current == shouldGlow) {
            return false;
        }

        entity.set(ComponentTypes.GLOW, new GlowComponent(shouldGlow));
        return true;
    }
}

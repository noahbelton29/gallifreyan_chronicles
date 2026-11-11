package com.noahtnt2009.gallifreyan_chronicles.tardis.mind.mood;

import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.TardisMind;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.TardisMindSystem;

import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisExteriorBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.tardis.api.Tardis;
import com.noahtnt2009.gallifreyan_chronicles.tardis.api.TardisEvents;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.component.TardisSubsystem;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.system.SubsystemSystem;

public final class TardisMoods {
    private static final long OVERUSE_TICKS = 600L;
    private static final float OVERUSE_STRESS = 10f;
    private static final float DAMAGED_FLIGHT_STRESS = 8f;
    private static final float WRONG_KEY_STRESS = 4f;
    private static final float CALM_LANDING_STRESS = 30f;
    private static final float CALM_LANDING_TRUST = 1f;

    private TardisMoods() {
    }

    public static void init() {
        TardisEvents.TAKE_OFF.register(TardisMoods::onTakeOff);
        TardisEvents.LAND.register(TardisMoods::onLand);
    }

    public static void onWrongKey(Tardis tardis) {
        TardisMindSystem.addStress(tardis, WRONG_KEY_STRESS);
    }

    private static void onTakeOff(Tardis tardis) {
        if (tookOffTooSoon(tardis)) {
            TardisMindSystem.addStress(tardis, OVERUSE_STRESS);
        }
        if (hasDamagedSystems(tardis)) {
            TardisMindSystem.addStress(tardis, DAMAGED_FLIGHT_STRESS);
        }
    }

    private static void onLand(Tardis tardis) {
        if (TardisMindSystem.stress(tardis) < CALM_LANDING_STRESS) {
            TardisMindSystem.addTrust(tardis, CALM_LANDING_TRUST);
        }
        TardisMindSystem.markLanded(tardis);
    }

    private static boolean tookOffTooSoon(Tardis tardis) {
        long lastLand = TardisMindSystem.mind(tardis).lastLand();
        long now = TardisMindSystem.now(tardis.server());
        return lastLand != TardisMind.NEVER && now - lastLand < OVERUSE_TICKS;
    }

    private static boolean hasDamagedSystems(Tardis tardis) {
        TardisExteriorBlockEntity exterior = tardis.exteriorIfLoaded().orElse(null);
        if (exterior == null) return false;
        for (TardisSubsystem subsystem : TardisSubsystem.values()) {
            if (SubsystemSystem.isFaulty(exterior.asEntity(), subsystem)) return true;
        }
        return false;
    }
}

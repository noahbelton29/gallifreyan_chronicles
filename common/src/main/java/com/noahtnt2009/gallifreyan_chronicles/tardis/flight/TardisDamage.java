package com.noahtnt2009.gallifreyan_chronicles.tardis.flight;

import com.noahtnt2009.gallifreyan_chronicles.tardis.api.Tardis;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.mood.TardisAlarm;

public final class TardisDamage {
    private TardisDamage() {
    }

    public static void markCrashed(Tardis tardis) {
        tardis.data().setCrashed(tardis.id(), true);
        TardisAlarm.untilRepaired(tardis);
        CrashSmoke.start(tardis);
    }

    public static void repair(Tardis tardis) {
        tardis.data().setCrashed(tardis.id(), false);
    }

    public static boolean isCrashed(Tardis tardis) {
        return tardis.data().isCrashed(tardis.id());
    }
}

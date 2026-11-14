package uk.nbforge.gallifreyan_chronicles.core.lifecycle;

import uk.nbforge.gallifreyan_chronicles.util.GCSchedulerUtil;

import uk.nbforge.gallifreyan_chronicles.tardis.flight.crash.CrashSmoke;
import uk.nbforge.gallifreyan_chronicles.tardis.flight.crash.TardisCrash;

import uk.nbforge.gallifreyan_chronicles.loader.GCJsonReloadListener;
import uk.nbforge.gallifreyan_chronicles.tardis.control.impl.FlightLeverControl;
import uk.nbforge.gallifreyan_chronicles.tardis.ecs.system.ConsoleSyncSystem;
import uk.nbforge.gallifreyan_chronicles.tardis.ecs.system.DoorSyncSystem;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.TardisMindSystem;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.mood.TardisAlarm;
import uk.nbforge.gallifreyan_chronicles.tardis.security.IsomorphicPunishment;

public final class ServerLifecycleHandler {
    private ServerLifecycleHandler() {
    }

    public static void onServerStart() {
        GCSchedulerUtil.clear();
        IsomorphicPunishment.reset();
        FlightLeverControl.resetState();
        TardisMindSystem.resetState();
    }

    public static void onServerStop() {
        GCSchedulerUtil.clear();
        FlightLeverControl.resetState();
        TardisMindSystem.resetState();
        TardisAlarm.reset();
        TardisCrash.reset();
        CrashSmoke.reset();
        DoorSyncSystem.clear();
        IsomorphicPunishment.reset();
        ConsoleSyncSystem.clear();
        GCJsonReloadListener.setCurrentServer(null);
    }
}

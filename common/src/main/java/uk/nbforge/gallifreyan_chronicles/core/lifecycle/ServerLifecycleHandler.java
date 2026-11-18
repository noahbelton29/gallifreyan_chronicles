package uk.nbforge.gallifreyan_chronicles.core.lifecycle;

import uk.nbforge.gallifreyan_chronicles.loader.GCJsonReloadListener;
import uk.nbforge.gallifreyan_chronicles.tardis.console.ConsoleSync;
import uk.nbforge.gallifreyan_chronicles.tardis.door.DoorSync;
import uk.nbforge.gallifreyan_chronicles.tardis.flight.TardisAutopilot;
import uk.nbforge.gallifreyan_chronicles.tardis.flight.TardisFlight;
import uk.nbforge.gallifreyan_chronicles.tardis.flight.crash.CrashSmoke;
import uk.nbforge.gallifreyan_chronicles.tardis.flight.crash.TardisCrash;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.TardisMinds;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.mood.TardisAlarm;
import uk.nbforge.gallifreyan_chronicles.tardis.security.IsomorphicPunishment;
import uk.nbforge.gallifreyan_chronicles.util.SchedulerUtil;

public final class ServerLifecycleHandler {
    private ServerLifecycleHandler() {}

    public static void onServerStart() {
        SchedulerUtil.clear();
        IsomorphicPunishment.reset();
        TardisFlight.resetState();
        TardisMinds.resetState();
        TardisAutopilot.reset();
    }

    public static void onServerStop() {
        SchedulerUtil.clear();
        TardisFlight.resetState();
        TardisMinds.resetState();
        TardisAutopilot.reset();
        TardisAlarm.reset();
        TardisCrash.reset();
        CrashSmoke.reset();
        DoorSync.clear();
        IsomorphicPunishment.reset();
        ConsoleSync.clear();
        GCJsonReloadListener.setCurrentServer(null);
        ServerState.clearAll();
    }
}

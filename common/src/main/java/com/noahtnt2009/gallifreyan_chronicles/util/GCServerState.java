package com.noahtnt2009.gallifreyan_chronicles.util;

import com.noahtnt2009.gallifreyan_chronicles.tardis.flight.TardisCrash;

import com.noahtnt2009.gallifreyan_chronicles.data.GCJsonReloadListener;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.impl.FlightLeverControl;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.system.ConsoleSyncSystem;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.system.DoorSyncSystem;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.TardisMindSystem;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.mood.TardisAlarm;
import com.noahtnt2009.gallifreyan_chronicles.tardis.security.IsomorphicPunishment;

public final class GCServerState {
    private GCServerState() {
    }

    public static void onServerStart() {
        GCScheduler.clear();
        IsomorphicPunishment.reset();
        FlightLeverControl.resetState();
        TardisMindSystem.resetState();
    }

    public static void onServerStop() {
        GCScheduler.clear();
        FlightLeverControl.resetState();
        TardisMindSystem.resetState();
        TardisAlarm.reset();
        TardisCrash.reset();
        DoorSyncSystem.clear();
        IsomorphicPunishment.reset();
        ConsoleSyncSystem.clear();
        GCJsonReloadListener.setCurrentServer(null);
    }
}

package com.noahtnt2009.gallifreyan_chronicles.util;

import com.noahtnt2009.gallifreyan_chronicles.data.GCJsonReloadListener;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.impl.FlightLeverControl;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.system.ConsoleSyncSystem;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.system.DoorSyncSystem;

public final class GCServerState {
    private GCServerState() {
    }

    public static void onServerStart() {
        GCScheduler.clear();
        FlightLeverControl.resetState();
    }

    public static void onServerStop() {
        GCScheduler.clear();
        FlightLeverControl.resetState();
        DoorSyncSystem.clear();
        ConsoleSyncSystem.clear();
        GCJsonReloadListener.setCurrentServer(null);
    }
}

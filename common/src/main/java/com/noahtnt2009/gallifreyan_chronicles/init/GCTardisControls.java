package com.noahtnt2009.gallifreyan_chronicles.init;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.impl.FlightLeverControl;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.TardisControlRegistry;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.impl.HandbrakeControl;

public class GCTardisControls {
    public static void registerTardisControls() {
        TardisControlRegistry.register("flight_lever", new FlightLeverControl());
        TardisControlRegistry.register("handbrake", new HandbrakeControl());

        Constants.LOG.info("Registered GC TARDIS Controls");
    }
}

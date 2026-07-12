package com.noahtnt2009.gallifreyan_chronicles.init;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.FlightLeverControl;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.TardisControlRegistry;

public class GCTardisControls {
    public static void registerTardisControls() {
        TardisControlRegistry.register("flight_lever", new FlightLeverControl());
        Constants.LOG.info("GC Registered TARDIS Controls");
    }
}

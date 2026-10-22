package com.noahtnt2009.gallifreyan_chronicles.init;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.impl.DoorControl;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.impl.DoorLockControl;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.impl.FlightLeverControl;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.TardisControlRegistry;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.impl.HandbrakeControl;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.impl.PowerLeverControl;

public class GCTardisControls {
    public static void registerTardisControls() {
        TardisControlRegistry.register("flight_lever", new FlightLeverControl());
        TardisControlRegistry.register("handbrake", new HandbrakeControl());
        TardisControlRegistry.register("power_lever", new PowerLeverControl());
        TardisControlRegistry.register("door_control", new DoorControl());
        TardisControlRegistry.register(DoorLockControl.ID, new DoorLockControl());

        Constants.LOG.info("Registered GC TARDIS Controls");
    }
}

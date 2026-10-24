package com.noahtnt2009.gallifreyan_chronicles.init;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.impl.DoorControl;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.impl.DoorLockControl;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.impl.FlightLeverControl;
import com.mojang.serialization.JsonOps;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.NoopControl;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.TardisControl;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.TardisControlRegistry;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.TardisControlTypes;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.impl.HandbrakeControl;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.impl.PowerLeverControl;

public class GCTardisControls {
    public static void registerTardisControls() {
        // control kinds, each with the codec for its settings
        TardisControlTypes.register("noop", NoopControl.CODEC);
        TardisControlTypes.register("flight_lever", FlightLeverControl.CODEC);
        TardisControlTypes.register("handbrake", HandbrakeControl.CODEC);
        TardisControlTypes.register("power_lever", PowerLeverControl.CODEC);
        TardisControlTypes.register("door_control", DoorControl.CODEC);
        TardisControlTypes.register(DoorLockControl.ID, DoorLockControl.CODEC);

        // the controls on a console, by control id
        TardisControlRegistry.register("flight_lever", new FlightLeverControl());
        TardisControlRegistry.register("handbrake", new HandbrakeControl());
        TardisControlRegistry.register("power_lever", new PowerLeverControl());
        TardisControlRegistry.register("door_control", new DoorControl());
        TardisControlRegistry.register(DoorLockControl.ID, new DoorLockControl());

        verifyCodecs();
        Constants.LOG.info("Registered GC TARDIS Controls");
    }

    // writes every registered control out and reads it back, so a broken codec shows up in the log at startup
    private static void verifyCodecs() {
        for (String id : TardisControlRegistry.allIds()) {
            TardisControl control = TardisControlRegistry.get(id);
            TardisControlTypes.CODEC.encodeStart(JsonOps.INSTANCE, control)
                    .flatMap(json -> TardisControlTypes.CODEC.parse(JsonOps.INSTANCE, json))
                    .resultOrPartial(error -> Constants.LOG.warn("TARDIS control '{}' failed its codec round trip: {}", id, error));
        }
    }
}

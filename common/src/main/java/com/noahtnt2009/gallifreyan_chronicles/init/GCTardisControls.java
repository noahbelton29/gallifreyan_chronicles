package com.noahtnt2009.gallifreyan_chronicles.init;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.impl.DoorControl;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.impl.DoorLockControl;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.impl.FlightLeverControl;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.NoopControl;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.TardisControl;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.TardisControlRegistry;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.TardisControlTypes;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.impl.HandbrakeControl;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.impl.PowerLeverControl;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.impl.ChangeIncrementControl;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.impl.DimensionSelectorControl;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.impl.XIncrementControl;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.impl.YIncrementControl;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.impl.ZIncrementControl;

public class GCTardisControls {
    public static final String NOOP_CONTROL = "noop";
    public static final String FLIGHT_LEVER = "flight_lever";
    public static final String HANDBRAKE = "handbrake";
    public static final String POWER_LEVER = "power_lever";
    public static final String DOOR_CONTROL = "door_control";
    public static final String DOOR_LOCK_CONTROL = "door_lock";
    public static final String X_INCREMENT_CONTROL = "x_increment";
    public static final String Y_INCREMENT_CONTROL = "y_increment";
    public static final String Z_INCREMENT_CONTROL = "z_increment";
    public static final String CHANGE_INCREMENT = "change_increment";
    public static final String DIMENSION_SELECTOR = "dimension_selector";

    public static void registerTardisControls() {
        // every control goes through control(...), which registers its codec and its instance together,
        // so TardisControlRegistry holds all of them and the language provider can iterate it
        TardisControlTypes.register(NOOP_CONTROL, NoopControl.CODEC);
        control(FLIGHT_LEVER, FlightLeverControl.CODEC, new FlightLeverControl());
        control(HANDBRAKE, HandbrakeControl.CODEC, new HandbrakeControl());
        control(POWER_LEVER, PowerLeverControl.CODEC, new PowerLeverControl());
        control(DOOR_CONTROL, DoorControl.CODEC, new DoorControl());
        control(DOOR_LOCK_CONTROL, DoorLockControl.CODEC, new DoorLockControl());
        control(X_INCREMENT_CONTROL, XIncrementControl.CODEC, new XIncrementControl());
        control(Y_INCREMENT_CONTROL, YIncrementControl.CODEC, new YIncrementControl());
        control(Z_INCREMENT_CONTROL, ZIncrementControl.CODEC, new ZIncrementControl());
        control(CHANGE_INCREMENT, ChangeIncrementControl.CODEC, new ChangeIncrementControl());
        control(DIMENSION_SELECTOR, DimensionSelectorControl.CODEC, new DimensionSelectorControl());

        verifyCodecs();
        Constants.LOG.info("Registered GC TARDIS Controls");
    }

    private static <T extends TardisControl> void control(String id, MapCodec<T> codec, T instance) {
        TardisControlTypes.register(id, codec);
        TardisControlRegistry.register(id, instance);
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

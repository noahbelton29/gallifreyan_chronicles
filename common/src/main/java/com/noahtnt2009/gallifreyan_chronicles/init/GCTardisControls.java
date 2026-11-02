package com.noahtnt2009.gallifreyan_chronicles.init;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.impl.*;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.NoopControl;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.TardisControl;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.TardisControlRegistry;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.TardisControlTypes;

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
    public static final String ISOMORPHIC_CONTROL = "isomorphic_control";
    public static final String DIRECTION_CONTROL = "direction_control";
    public static final String CLOAK_CONTROL = "cloak_control";

    public static void registerTardisControls() {
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
        control(ISOMORPHIC_CONTROL, IsomorphicControl.CODEC, new IsomorphicControl());
        control(DIRECTION_CONTROL, DirectionControl.CODEC, new DirectionControl());
        control(CLOAK_CONTROL, CloakControl.CODEC, new CloakControl());

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

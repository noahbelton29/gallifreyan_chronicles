package uk.nbforge.gallifreyan_chronicles.core;

import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.tardis.control.impl.*;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import uk.nbforge.gallifreyan_chronicles.tardis.codec.control.TardisControlCodec;
import uk.nbforge.gallifreyan_chronicles.tardis.control.ControlPower;
import uk.nbforge.gallifreyan_chronicles.tardis.control.NoopControl;
import uk.nbforge.gallifreyan_chronicles.tardis.control.TardisControl;
import uk.nbforge.gallifreyan_chronicles.core.dynamic_registries.TardisControlRegistry;
import uk.nbforge.gallifreyan_chronicles.tardis.control.TardisControlTypes;
import uk.nbforge.gallifreyan_chronicles.tardis.control.BaseControl;
import uk.nbforge.gallifreyan_chronicles.tardis.control.ControlAnimations;
import java.util.function.Function;

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
    public static final String INTERIOR_DOOR_CONTROL = "interior_door_control";

    public static void registerTardisControls() {
        TardisControlTypes.register(NOOP_CONTROL, NoopControl.class, MapCodec.unit(NoopControl.INSTANCE));
        control(FLIGHT_LEVER, FlightLeverControl.DEFAULT_ANIMATIONS, FlightLeverControl::new);
        control(HANDBRAKE, HandbrakeControl.DEFAULT_ANIMATIONS, HandbrakeControl::new);
        control(POWER_LEVER, PowerLeverControl.DEFAULT_ANIMATIONS, PowerLeverControl::new);
        control(DOOR_CONTROL, DoorControl.DEFAULT_ANIMATIONS, DoorControl::new);
        control(DOOR_LOCK_CONTROL, DoorLockControl.DEFAULT_ANIMATIONS, DoorLockControl::new);
        control(X_INCREMENT_CONTROL, XIncrementControl.DEFAULT_ANIMATIONS, XIncrementControl::new);
        control(Y_INCREMENT_CONTROL, YIncrementControl.DEFAULT_ANIMATIONS, YIncrementControl::new);
        control(Z_INCREMENT_CONTROL, ZIncrementControl.DEFAULT_ANIMATIONS, ZIncrementControl::new);
        control(CHANGE_INCREMENT, ChangeIncrementControl.DEFAULT_ANIMATIONS, ChangeIncrementControl::new);
        control(DIMENSION_SELECTOR, DimensionSelectorControl.DEFAULT_ANIMATIONS, DimensionSelectorControl::new);
        control(ISOMORPHIC_CONTROL, IsomorphicControl.DEFAULT_ANIMATIONS, IsomorphicControl::new);
        control(DIRECTION_CONTROL, DirectionControl.DEFAULT_ANIMATIONS, DirectionControl::new);
        control(CLOAK_CONTROL, CloakControl.DEFAULT_ANIMATIONS, CloakControl::new);
        control(INTERIOR_DOOR_CONTROL, InteriorDoorControl.DEFAULT_ANIMATIONS, InteriorDoorControl::new);

        verifyCodecs();
        ControlPower.validate();
        Constants.LOG.info("Registered GC TARDIS Controls");
    }

    /* the codec is built from the control's default animations, so each control only needs those and a constructor */
    private static <T extends BaseControl> void control(String id, ControlAnimations defaults, Function<ControlAnimations, T> ctor) {
        T instance = ctor.apply(defaults);
        TardisControlTypes.register(id, instance.getClass(), BaseControl.codec(defaults, ctor));
        TardisControlRegistry.register(id, instance);
    }

    // writes every registered control out and reads it back, so a broken codec shows up in the log at startup
    private static void verifyCodecs() {
        for (String id : TardisControlRegistry.allIds()) {
            TardisControl control = TardisControlRegistry.get(id);
            TardisControlCodec.CODEC.encodeStart(JsonOps.INSTANCE, control)
                    .flatMap(json -> TardisControlCodec.CODEC.parse(JsonOps.INSTANCE, json))
                    .resultOrPartial(error -> Constants.LOG.warn("TARDIS control '{}' failed its codec round trip: {}", id, error));
        }
    }
}

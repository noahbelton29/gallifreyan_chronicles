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
    public static final String SPACE_TIME_THROTTLE = "space_time_throttle";
    public static final String TIME_ROTOR_HANDBRAKE = "time_rotor_handbrake";
    public static final String POWER_CONTROL = "power_control";
    public static final String DOOR_CONTROL = "door_control";
    public static final String DOOR_LOCK_CONTROL = "door_lock_control";
    public static final String X_INCREMENT_CONTROL = "x_increment";
    public static final String Y_INCREMENT_CONTROL = "y_increment";
    public static final String Z_INCREMENT_CONTROL = "z_increment";
    public static final String COORDINATE_STEP_CONTROL = "coordinate_step_control";
    public static final String DIMENSIONAL_CONTROL = "dimensional_control";
    public static final String ISOMORPHIC_CONTROL = "isomorphic_control";
    public static final String DIRECTIONAL_CONTROL = "directional_control";
    public static final String CLOAKING_DEVICE_CONTROL = "cloaking_device_control";
    public static final String INTERIOR_DOOR_CONTROL = "interior_door_control";
    public static final String GYROSCOPIC_STABILIZERS = "gyroscopic_stabilizers";
    public static final String CLOISTER_BELL_CONTROL = "cloister_bell_control";

    public static void registerTardisControls() {
        TardisControlTypes.register(NOOP_CONTROL, NoopControl.class, MapCodec.unit(NoopControl.INSTANCE));
        control(SPACE_TIME_THROTTLE, SpaceTimeThrottleControl.DEFAULT_ANIMATIONS, SpaceTimeThrottleControl::new);
        control(TIME_ROTOR_HANDBRAKE, TimeRotorHandbrakeControl.DEFAULT_ANIMATIONS, TimeRotorHandbrakeControl::new);
        control(POWER_CONTROL, PowerControl.DEFAULT_ANIMATIONS, PowerControl::new);
        control(DOOR_CONTROL, DoorControl.DEFAULT_ANIMATIONS, DoorControl::new);
        control(DOOR_LOCK_CONTROL, DoorLockControl.DEFAULT_ANIMATIONS, DoorLockControl::new);
        control(X_INCREMENT_CONTROL, XIncrementControl.DEFAULT_ANIMATIONS, XIncrementControl::new);
        control(Y_INCREMENT_CONTROL, YIncrementControl.DEFAULT_ANIMATIONS, YIncrementControl::new);
        control(Z_INCREMENT_CONTROL, ZIncrementControl.DEFAULT_ANIMATIONS, ZIncrementControl::new);
        control(COORDINATE_STEP_CONTROL, CoordinateStepControl.DEFAULT_ANIMATIONS, CoordinateStepControl::new);
        control(DIMENSIONAL_CONTROL, DimensionalControl.DEFAULT_ANIMATIONS, DimensionalControl::new);
        control(ISOMORPHIC_CONTROL, IsomorphicControl.DEFAULT_ANIMATIONS, IsomorphicControl::new);
        control(DIRECTIONAL_CONTROL, DirectionalControl.DEFAULT_ANIMATIONS, DirectionalControl::new);
        control(CLOAKING_DEVICE_CONTROL, CloakingDeviceControl.DEFAULT_ANIMATIONS, CloakingDeviceControl::new);
        control(INTERIOR_DOOR_CONTROL, InteriorDoorControl.DEFAULT_ANIMATIONS, InteriorDoorControl::new);
        control(GYROSCOPIC_STABILIZERS, GyroscopicStabilizersControl.DEFAULT_ANIMATIONS, GyroscopicStabilizersControl::new);
        control(CLOISTER_BELL_CONTROL, CloisterBellControl.DEFAULT_ANIMATIONS, CloisterBellControl::new);

        verifyCodecs();
        ControlPower.validate();
        Constants.LOG.info("Registered GC TARDIS Controls");
    }

    /**
     * The codec is built from the default animations of the control so each control only needs those and a constructor.
     */
    private static <T extends BaseControl> void control(String id, ControlAnimations defaults, Function<ControlAnimations, T> ctor) {
        T instance = ctor.apply(defaults);
        TardisControlTypes.register(id, instance.getClass(), BaseControl.codec(defaults, ctor));
        TardisControlRegistry.register(id, instance);
    }

    /**
     * Writes every registered control out and reads it back so a broken codec shows up in the log at startup.
     */
    private static void verifyCodecs() {
        for (String id : TardisControlRegistry.allIds()) {
            TardisControl control = TardisControlRegistry.get(id);
            TardisControlCodec.CODEC.encodeStart(JsonOps.INSTANCE, control)
                    .flatMap(json -> TardisControlCodec.CODEC.parse(JsonOps.INSTANCE, json))
                    .resultOrPartial(error -> Constants.LOG.warn("TARDIS control '{}' failed its codec round trip: {}", id, error));
        }
    }
}

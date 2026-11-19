package uk.nbforge.gallifreyan_chronicles.core;

import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import uk.nbforge.gallifreyan_chronicles.tardis.control.*;
import uk.nbforge.gallifreyan_chronicles.tardis.control.impl.*;
import uk.nbforge.gallifreyan_chronicles.tardis.flight.TardisDestinations.Axis;
import uk.nbforge.gallifreyan_chronicles.util.GCLog;

import java.util.function.Function;

public class GCTardisControls {
    public static final String NOOP = "noop";
    public static final String SPACE_TIME_THROTTLE = "space_time_throttle";
    public static final String TIME_ROTOR_HANDBRAKE = "time_rotor_handbrake";
    public static final String POWER_CONTROL = "power_control";
    public static final String DOOR_CONTROL = "door_control";
    public static final String DOOR_LOCK_CONTROL = "door_lock_control";
    public static final String X_INCREMENT = "x_increment";
    public static final String Y_INCREMENT = "y_increment";
    public static final String Z_INCREMENT = "z_increment";
    public static final String COORDINATE_STEP_CONTROL = "coordinate_step_control";
    public static final String DIMENSIONAL_CONTROL = "dimensional_control";
    public static final String ISOMORPHIC_CONTROL = "isomorphic_control";
    public static final String DIRECTIONAL_CONTROL = "directional_control";
    public static final String CLOAKING_DEVICE_CONTROL = "cloaking_device_control";
    public static final String INTERIOR_DOOR_CONTROL = "interior_door_control";
    public static final String GYROSCOPIC_STABILIZERS = "gyroscopic_stabilizers";
    public static final String CLOISTER_BELL_CONTROL = "cloister_bell_control";
    public static final String NAVIGATIONAL_LOCK_CONTROL = "navigational_lock_control";
    public static final String NAVCOM_CONTROL = "navcom";

    public static void registerTardisControls() {
        TardisControls.registerType(NOOP, NoopControl.class, MapCodec.unit(NoopControl.INSTANCE));
        control(SPACE_TIME_THROTTLE, SpaceTimeThrottleControl::new);
        control(TIME_ROTOR_HANDBRAKE, TimeRotorHandbrakeControl::new);
        control(POWER_CONTROL, PowerControl::new);
        control(DOOR_CONTROL, DoorControl.DEFAULT_ANIMATIONS, DoorControl::new);
        control(DOOR_LOCK_CONTROL, DoorLockControl::new);
        axisControl(X_INCREMENT, Axis.X);
        axisControl(Y_INCREMENT, Axis.Y);
        axisControl(Z_INCREMENT, Axis.Z);
        control(COORDINATE_STEP_CONTROL, ControlAnimations.single(COORDINATE_STEP_CONTROL), CoordinateStepControl::new);
        control(DIMENSIONAL_CONTROL, ControlAnimations.single(DIMENSIONAL_CONTROL), DimensionalControl::new);
        control(ISOMORPHIC_CONTROL, IsomorphicControl::new);
        control(DIRECTIONAL_CONTROL, DirectionalControl::new);
        control(
            CLOAKING_DEVICE_CONTROL,
            ControlAnimations.toggle(CLOAKING_DEVICE_CONTROL, true),
            CloakingDeviceControl::new);
        control(INTERIOR_DOOR_CONTROL, InteriorDoorControl::new);
        control(
            GYROSCOPIC_STABILIZERS,
            ControlAnimations.toggle(GYROSCOPIC_STABILIZERS, true),
            GyroscopicStabilizersControl::new);
        control(CLOISTER_BELL_CONTROL, CloisterBellControl::new);
        control(NAVIGATIONAL_LOCK_CONTROL, NavigationalLockControl::new);
        control(NAVCOM_CONTROL, NavcomControl::new);

        verifyCodecs();
        ControlPower.validate();
        GCLog.CORE.info("Registered GC TARDIS Controls");
    }

    private static <T extends Control> void control(String id, Function<ControlAnimations, T> ctor) {
        control(id, ControlAnimations.toggle(id), ctor);
    }

    private static <T extends Control> void control(
        String id, ControlAnimations defaults, Function<ControlAnimations, T> ctor) {
        T instance = ctor.apply(defaults);
        TardisControls.register(id, instance.getClass(), Control.codec(defaults, ctor), instance);
    }

    private static void axisControl(String id, Axis axis) {
        AxisIncrementControl instance = new AxisIncrementControl(axis, AxisIncrementControl.defaultAnimations(axis));
        TardisControls.register(id, AxisIncrementControl.codec(axis), instance);
    }

    private static void verifyCodecs() {
        for (String id : TardisControls.ids()) {
            TardisControl control = TardisControls.get(id);
            TardisControl.CODEC
                .encodeStart(JsonOps.INSTANCE, control)
                .flatMap(json -> TardisControl.CODEC.parse(JsonOps.INSTANCE, json))
                .resultOrPartial(
                    error -> GCLog.CORE.warn("TARDIS control '{}' failed its codec round trip: {}", id, error));
        }
    }
}

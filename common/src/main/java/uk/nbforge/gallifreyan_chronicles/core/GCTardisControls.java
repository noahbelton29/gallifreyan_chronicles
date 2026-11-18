package uk.nbforge.gallifreyan_chronicles.core;

import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import java.util.function.Function;
import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.tardis.control.BaseControl;
import uk.nbforge.gallifreyan_chronicles.tardis.control.ControlAnimations;
import uk.nbforge.gallifreyan_chronicles.tardis.control.ControlPower;
import uk.nbforge.gallifreyan_chronicles.tardis.control.NoopControl;
import uk.nbforge.gallifreyan_chronicles.tardis.control.TardisControl;
import uk.nbforge.gallifreyan_chronicles.tardis.control.TardisControls;
import uk.nbforge.gallifreyan_chronicles.tardis.control.impl.AxisIncrementControl;
import uk.nbforge.gallifreyan_chronicles.tardis.control.impl.CloakingDeviceControl;
import uk.nbforge.gallifreyan_chronicles.tardis.control.impl.CloisterBellControl;
import uk.nbforge.gallifreyan_chronicles.tardis.control.impl.CoordinateStepControl;
import uk.nbforge.gallifreyan_chronicles.tardis.control.impl.DimensionalControl;
import uk.nbforge.gallifreyan_chronicles.tardis.control.impl.DirectionalControl;
import uk.nbforge.gallifreyan_chronicles.tardis.control.impl.DoorControl;
import uk.nbforge.gallifreyan_chronicles.tardis.control.impl.DoorLockControl;
import uk.nbforge.gallifreyan_chronicles.tardis.control.impl.GyroscopicStabilizersControl;
import uk.nbforge.gallifreyan_chronicles.tardis.control.impl.InteriorDoorControl;
import uk.nbforge.gallifreyan_chronicles.tardis.control.impl.IsomorphicControl;
import uk.nbforge.gallifreyan_chronicles.tardis.control.impl.PowerControl;
import uk.nbforge.gallifreyan_chronicles.tardis.control.impl.SpaceTimeThrottleControl;
import uk.nbforge.gallifreyan_chronicles.tardis.control.impl.TimeRotorHandbrakeControl;
import uk.nbforge.gallifreyan_chronicles.tardis.flight.TardisDestinations.Axis;

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

    public static void registerTardisControls() {
        TardisControls.registerType(NOOP, NoopControl.class, MapCodec.unit(NoopControl.INSTANCE));
        control(SPACE_TIME_THROTTLE, SpaceTimeThrottleControl.DEFAULT_ANIMATIONS, SpaceTimeThrottleControl::new);
        control(TIME_ROTOR_HANDBRAKE, TimeRotorHandbrakeControl.DEFAULT_ANIMATIONS, TimeRotorHandbrakeControl::new);
        control(POWER_CONTROL, PowerControl.DEFAULT_ANIMATIONS, PowerControl::new);
        control(DOOR_CONTROL, DoorControl.DEFAULT_ANIMATIONS, DoorControl::new);
        control(DOOR_LOCK_CONTROL, DoorLockControl.DEFAULT_ANIMATIONS, DoorLockControl::new);
        axisControl(X_INCREMENT, Axis.X);
        axisControl(Y_INCREMENT, Axis.Y);
        axisControl(Z_INCREMENT, Axis.Z);
        control(COORDINATE_STEP_CONTROL, CoordinateStepControl.DEFAULT_ANIMATIONS, CoordinateStepControl::new);
        control(DIMENSIONAL_CONTROL, DimensionalControl.DEFAULT_ANIMATIONS, DimensionalControl::new);
        control(ISOMORPHIC_CONTROL, IsomorphicControl.DEFAULT_ANIMATIONS, IsomorphicControl::new);
        control(DIRECTIONAL_CONTROL, DirectionalControl.DEFAULT_ANIMATIONS, DirectionalControl::new);
        control(CLOAKING_DEVICE_CONTROL, CloakingDeviceControl.DEFAULT_ANIMATIONS, CloakingDeviceControl::new);
        control(INTERIOR_DOOR_CONTROL, InteriorDoorControl.DEFAULT_ANIMATIONS, InteriorDoorControl::new);
        control(
                GYROSCOPIC_STABILIZERS,
                GyroscopicStabilizersControl.DEFAULT_ANIMATIONS,
                GyroscopicStabilizersControl::new);
        control(CLOISTER_BELL_CONTROL, CloisterBellControl.DEFAULT_ANIMATIONS, CloisterBellControl::new);

        verifyCodecs();
        ControlPower.validate();
        Constants.LOG.info("Registered GC TARDIS Controls");
    }

    private static <T extends BaseControl> void control(
            String id, ControlAnimations defaults, Function<ControlAnimations, T> ctor) {
        T instance = ctor.apply(defaults);
        TardisControls.register(id, instance.getClass(), BaseControl.codec(defaults, ctor), instance);
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
                    .resultOrPartial(error ->
                            Constants.LOG.warn("TARDIS control '{}' failed its codec round trip: {}", id, error));
        }
    }
}

package uk.nbforge.gallifreyan_chronicles.tardis.console.animation;

import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.animatable.manager.AnimatableManager;
import uk.nbforge.gallifreyan_chronicles.core.block_entities.TardisConsoleBlockEntity;
import uk.nbforge.gallifreyan_chronicles.core.dynamic_registries.TardisControlRegistry;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public final class ConsoleControllers {
    public static final String IDLE_STATE = "rotor_idle";
    public static final String FLIGHT_STATE = "rotor_flight";

    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop(IDLE_STATE);
    private static final RawAnimation FLIGHT_ANIM = RawAnimation.begin().thenLoop(FLIGHT_STATE);
    private static final double SNAP_TO_END = 10_000.0;
    private static final int SNAP_FRAMES = 2;

    private final Map<String, Integer> snapFrames = new HashMap<>();
    private Map<String, AnimationController<TardisConsoleBlockEntity>> controlControllers = Map.of();
    private AnimationController<TardisConsoleBlockEntity> rotorController;

    public void register(AnimatableManager.ControllerRegistrar registrar, BooleanSupplier hasPower,
                         Supplier<String> rotorState) {
        this.rotorController = new AnimationController<>("rotor", 15, test -> {
            if (!hasPower.getAsBoolean()) {
                if (test.controller().getCurrentRawAnimation() == null) {
                    return test.setAndContinue(IDLE_ANIM);
                }
                return PlayState.PAUSE;
            }
            if (FLIGHT_STATE.equals(rotorState.get())) {
                return test.setAndContinue(FLIGHT_ANIM);
            }

            return test.setAndContinue(IDLE_ANIM);
        });

        Map<String, AnimationController<TardisConsoleBlockEntity>> built = new HashMap<>();
        for (String controlId : TardisControlRegistry.allIds()) {
            built.put(controlId, controlController(controlId));
        }
        this.controlControllers = Collections.unmodifiableMap(built);

        registrar.add(rotorController);
        for (AnimationController<TardisConsoleBlockEntity> controller : controlControllers.values()) {
            registrar.add(controller);
        }
    }

    private AnimationController<TardisConsoleBlockEntity> controlController(String controlId) {
        return new AnimationController<>(controlId, 0, test -> {
            if (test.controller().getCurrentRawAnimation() == null) {
                return PlayState.STOP;
            }
            Integer framesLeft = snapFrames.get(controlId);
            if (framesLeft != null) {
                test.controller().setAnimationTime(SNAP_TO_END);
                if (framesLeft <= 1) snapFrames.remove(controlId);
                else snapFrames.put(controlId, framesLeft - 1);
            }
            return PlayState.CONTINUE;
        });
    }

    public void requestSnap(String controlId) {
        snapFrames.put(controlId, SNAP_FRAMES);
    }

    public AnimationController<TardisConsoleBlockEntity> rotor() {
        return rotorController;
    }

    public Map<String, AnimationController<TardisConsoleBlockEntity>> controls() {
        return controlControllers;
    }
}

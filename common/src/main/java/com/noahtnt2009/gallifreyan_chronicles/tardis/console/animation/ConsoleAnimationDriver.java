package com.noahtnt2009.gallifreyan_chronicles.tardis.console.animation;

import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.network.TardisConsoleAnimationPayload;
import com.noahtnt2009.gallifreyan_chronicles.platform.Services;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ConsoleAnimationDriver implements ConsoleAnimatable {
    private static final String ROTOR = "rotor";
    private static final String IDLE_STATE = "ideal";
    private static final String FLIGHT_STATE = "flight";

    private final BlockPos pos;
    private Supplier<Level> levelSupplier;
    private Supplier<String> rotorStateSupplier = () -> IDLE_STATE;

    private @Nullable AnimationController<?> idleController;
    private @Nullable AnimationController<?> flightController;
    private Map<String, AnimationController<?>> controlControllers = Map.of();

    private final Map<String, String> lastAnimation = new HashMap<>();
    private final Map<String, Boolean> alternate = new HashMap<>();

    public ConsoleAnimationDriver(BlockPos pos) {
        this.pos = pos;
    }

    public void bind(Supplier<Level> levelSupplier,
                     Supplier<String> rotorStateSupplier,
                     AnimationController<?> idleController,
                     AnimationController<?> flightController,
                     Map<String, ? extends AnimationController<?>> controlControllers) {
        this.levelSupplier = levelSupplier;
        this.rotorStateSupplier = rotorStateSupplier;
        this.idleController = idleController;
        this.flightController = flightController;
        this.controlControllers = Map.copyOf(controlControllers);
    }

    private @Nullable Level level() {
        return levelSupplier != null ? levelSupplier.get() : null;
    }

    private @Nullable AnimationController<?> controllerFor(String controllerName) {
        if (ROTOR.equals(controllerName)) {
            String rotorState = rotorStateSupplier != null ? rotorStateSupplier.get() : IDLE_STATE;
            return FLIGHT_STATE.equals(rotorState) ? flightController : idleController;
        }
        return controlControllers.get(controllerName);
    }

    public void triggerAnimation(String controllerName, String animationName) {
        triggerAnimation(controllerName, animationName, null);
    }

    @Override
    public void triggerAnimation(String controllerName, String animationName, @Nullable Player triggeringPlayer) {
        Level level = level();
        if (level != null && level.isClientSide()) {
            applyAnimationLocally(controllerName, animationName);
        } else if (level instanceof ServerLevel serverLevel) {
            TardisConsoleAnimationPayload payload = new TardisConsoleAnimationPayload(pos, animationName, controllerName);
            Services.NETWORK.broadcastTardisConsoleAnimation(serverLevel.getServer(), payload);
        }
    }

    @Override
    public void applyAnimationLocally(String controllerName, String animationName) {
        Constants.LOG.info("Client triggering animation: {} on controller: {}", animationName, controllerName);
        if (ROTOR.equals(controllerName)) {
            return;
        }
        AnimationController<?> controller = controllerFor(controllerName);
        if (controller != null) {
            boolean repeat = animationName.equals(lastAnimation.put(controllerName, animationName));
            boolean flip = repeat && !alternate.getOrDefault(controllerName, false);
            alternate.put(controllerName, flip);
            RawAnimation raw = RawAnimation.begin().thenPlay(animationName);
            controller.setAnimation(flip ? raw.thenWait(1) : raw);
        } else {
            Constants.LOG.warn("No animation controller found for control '{}', is it registered in TardisControlRegistry?", controllerName);
        }
    }
}
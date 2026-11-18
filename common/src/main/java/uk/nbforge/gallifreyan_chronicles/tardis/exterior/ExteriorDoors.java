package uk.nbforge.gallifreyan_chronicles.tardis.exterior;

import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;
import uk.nbforge.gallifreyan_chronicles.core.block_entity.TardisExteriorBlockEntity;
import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import uk.nbforge.gallifreyan_chronicles.tardis.door.DoorMotion;
import uk.nbforge.gallifreyan_chronicles.tardis.door.DoorState;
import uk.nbforge.gallifreyan_chronicles.tardis.door.DoorStatus;
import uk.nbforge.gallifreyan_chronicles.tardis.door.DoorSync;
import uk.nbforge.gallifreyan_chronicles.tardis.exterior.effect.ExteriorSounds;
import uk.nbforge.gallifreyan_chronicles.util.SavedValue;

public final class ExteriorDoors {
    public static final String ANIM_CONTROLLER = "anim_controller";

    // "snapping" is just playing the anim stupidly fast for a few frames
    private static final int SNAP_SPEED = 999999;
    private static final int SNAP_FRAMES = 3;

    private final TardisExteriorBlockEntity exterior;
    private final SavedValue<DoorStatus> door = new SavedValue<>("door", DoorStatus.CODEC, DoorStatus::closed);
    private @Nullable AnimationController<TardisExteriorBlockEntity> controller;
    private int doorAnimSeq = 0;
    private String doorAnim = "";
    private boolean doorAnimSeqKnown = false;
    private boolean doorAnimInstant = false;
    private int snapFramesLeft = 0;
    private boolean needsStateSync = false;

    public ExteriorDoors(TardisExteriorBlockEntity exterior) {
        this.exterior = exterior;
    }

    private static String animationFor(DoorState state) {
        return switch (state) {
            case CLOSED -> "close_all";
            case LEFT_OPEN -> "open_left";
            case RIGHT_OPEN -> "open_right";
            case BOTH_OPEN -> "open_all";
        };
    }

    private boolean isClientSide() {
        Level level = exterior.getLevel();
        return level != null && level.isClientSide();
    }

    public DoorState getState() {
        return door.get().state();
    }

    public boolean areFullyClosed() {
        return getState().isClosed() && !door.get().isAnimating(System.currentTimeMillis());
    }

    public boolean areSealedForTravel() {
        return exterior.travel().isTravelling() && getState().isClosed();
    }

    public void interact(boolean sneaking) {
        interact(sneaking, false);
    }

    public void interact(boolean sneaking, boolean ignoreLock) {
        if (areSealedForTravel()) {
            exterior.lock().announceSealed();
            return;
        }
        if (!ignoreLock && exterior.lock().isLocked()) {
            return;
        }
        DoorMotion.InteractionResult result = DoorMotion.interact(door.get(), sneaking, System.currentTimeMillis());
        if (result.next() != null) {
            door.set(result.next());
            exterior.sync();
        }
        if (result.hasNoAnimation()) return;
        if (controller != null) {
            controller.setAnimationSpeed(1.0);
        }
        playAnimation(result.animationName());
        ExteriorSounds.door(exterior.getLevel(), exterior.getBlockPos(), result.opening());
        exterior.sync();
        if (exterior.getLevel() instanceof ServerLevel serverLevel) {
            UUID id = exterior.getTardisId();
            if (id != null) {
                Tardis.get(serverLevel.getServer(), id)
                        .ifPresent(tardis -> DoorSync.onExteriorDoorChanged(
                                tardis, result.state(), result.animationName(), result.opening()));
            }
        }
    }

    public void applySyncedDoor(DoorState state, String animation, boolean opening) {
        door.set(DoorMotion.applyRemoteDoorState(door.get(), state, opening, System.currentTimeMillis()));
        exterior.sync();
        if (controller != null) {
            controller.setAnimationSpeed(1.0);
        }
        playAnimation(animation);
        ExteriorSounds.door(exterior.getLevel(), exterior.getBlockPos(), opening);
        exterior.sync();
    }

    public void snapShut() {
        if (exterior.getLevel() == null || isClientSide()) return;
        door.set(DoorMotion.snapToState(door.get(), DoorState.CLOSED));
        exterior.sync();
        doorAnimSeq++;
        doorAnim = animationFor(DoorState.CLOSED);
        doorAnimInstant = true;
        exterior.sync();
    }

    public void resetAnimationState() {
        needsStateSync = true;
        if (isClientSide()) {
            snapToState();
        } else {
            reapplyVisualState();
        }
        exterior.sync();
    }

    private void reapplyVisualState() {
        if (isClientSide()) return;
        doorAnimSeq++;
        doorAnim = animationFor(getState());
        doorAnimInstant = true;
        exterior.sync();
    }

    private void snapToState() {
        if (!isClientSide() || controller == null) return;
        snapAnimation(controller, animationFor(getState()));
    }

    private void snapAnimation(AnimationController<TardisExteriorBlockEntity> target, String animName) {
        target.setAnimation(RawAnimation.begin().thenPlay(animName));
        target.setAnimationSpeed(SNAP_SPEED);
        snapFramesLeft = SNAP_FRAMES;
    }

    private void startSnapClient() {
        if (controller == null) return;
        controller.setAnimationSpeed(SNAP_SPEED);
        snapFramesLeft = SNAP_FRAMES;
    }

    // server bumps the seq so clients play it once in load()
    public void playAnimation(String animationName) {
        if (isClientSide()) {
            exterior.triggerAnim(ANIM_CONTROLLER, animationName);
            return;
        }
        doorAnimSeq++;
        doorAnim = animationName;
        doorAnimInstant = false;
        exterior.sync();
    }

    public void onLevelSet(Level level) {
        if (level.isClientSide()) {
            needsStateSync = true;
        }
    }

    public AnimationController<TardisExteriorBlockEntity> createController() {
        controller = new AnimationController<>(ANIM_CONTROLLER, 0, state -> {
            if (needsStateSync && isClientSide()) {
                snapAnimation(state.controller(), animationFor(getState()));
                needsStateSync = false;
            } else if (snapFramesLeft > 0 && --snapFramesLeft == 0) {
                state.controller().setAnimationSpeed(1.0);
            }
            return PlayState.CONTINUE;
        });
        controller
                .triggerableAnim("open_left", RawAnimation.begin().thenPlay("open_left"))
                .triggerableAnim("open_right", RawAnimation.begin().thenPlay("open_right"))
                .triggerableAnim("open_all", RawAnimation.begin().thenPlay("open_all"))
                .triggerableAnim("close_left", RawAnimation.begin().thenPlay("close_left"))
                .triggerableAnim("close_right", RawAnimation.begin().thenPlay("close_right"))
                .triggerableAnim("close_left_shift", RawAnimation.begin().thenPlay("close_left_shift"))
                .triggerableAnim("close_right_shift", RawAnimation.begin().thenPlay("close_right_shift"))
                .triggerableAnim("close_all", RawAnimation.begin().thenPlay("close_all"));
        return controller;
    }

    public void afterControllersRegistered() {
        if (isClientSide()) {
            snapToState();
        }
    }

    public void copyFrom(ExteriorDoors other) {
        door.copyFrom(other.door);
    }

    public void save(ValueOutput output) {
        door.save(output);
        output.putInt("DoorAnimSeq", doorAnimSeq);
        output.putString("DoorAnim", doorAnim);
        output.putBoolean("DoorAnimInstant", doorAnimInstant);
    }

    public void load(ValueInput input) {
        door.load(input);
        int animSeq = input.getIntOr("DoorAnimSeq", 0);
        String anim = input.getStringOr("DoorAnim", "");
        boolean instant = input.getBooleanOr("DoorAnimInstant", false);
        if (doorAnimSeqKnown && animSeq != doorAnimSeq && !anim.isEmpty() && isClientSide()) {
            if (instant) {
                startSnapClient();
            } else if (controller != null) {
                controller.setAnimationSpeed(1.0);
            }
            exterior.triggerAnim(ANIM_CONTROLLER, anim);
        }
        doorAnimSeq = animSeq;
        doorAnim = anim;
        doorAnimInstant = instant;
        doorAnimSeqKnown = true;
    }
}

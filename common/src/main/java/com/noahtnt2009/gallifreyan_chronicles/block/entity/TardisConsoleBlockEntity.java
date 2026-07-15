package com.noahtnt2009.gallifreyan_chronicles.block.entity;

import com.geckolib.animatable.GeoBlockEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.util.GeckoLibUtil;
import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.client.sound.TardisConsoleHumSoundInstance;
import com.noahtnt2009.gallifreyan_chronicles.ecs.ComponentHolder;
import com.noahtnt2009.gallifreyan_chronicles.ecs.ComponentStore;
import com.noahtnt2009.gallifreyan_chronicles.ecs.Entity;
import com.noahtnt2009.gallifreyan_chronicles.entity.TardisControlEntity;
import com.noahtnt2009.gallifreyan_chronicles.network.TardisConsoleRotorStatePayload;
import com.noahtnt2009.gallifreyan_chronicles.platform.Services;
import com.noahtnt2009.gallifreyan_chronicles.tardis.console.animation.ConsoleAnimationDriver;
import com.noahtnt2009.gallifreyan_chronicles.tardis.console.control.ConsoleControlHost;
import com.noahtnt2009.gallifreyan_chronicles.tardis.console.control.ConsoleControlManager;
import com.noahtnt2009.gallifreyan_chronicles.tardis.console.TardisConsole;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.ControlSpec;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.TardisControlRegistry;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.TardisComponentTypes;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.TardisLinkable;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.system.ConsoleSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

public class TardisConsoleBlockEntity extends BlockEntity implements GeoBlockEntity, ComponentHolder, TardisLinkable, ConsoleControlHost {
    public static Supplier<BlockEntityType<TardisConsoleBlockEntity>> TYPE;
    private static final String IDLE_STATE = "rotor_idle";
    private static final String FLIGHT_STATE = "rotor_flight";

    private String currentRotorState = IDLE_STATE;

    private static final RawAnimation IDEAL_ANIM =
            RawAnimation.begin().thenLoop(IDLE_STATE);

    private static final RawAnimation FLIGHT_ANIM =
            RawAnimation.begin().thenLoop(FLIGHT_STATE);

    private Map<String, AnimationController<TardisConsoleBlockEntity>> controlControllers = Map.of();
    private AnimationController<TardisConsoleBlockEntity> idleController;
    private AnimationController<TardisConsoleBlockEntity> flightController;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final ComponentStore components = new ComponentStore();

    private final ConsoleAnimationDriver animationDriver;
    private final ConsoleControlManager controls;
    private boolean humStarted = false;

    public TardisConsoleBlockEntity(BlockPos pos, BlockState state) {
        super(TYPE.get(), pos, state);
        this.animationDriver = new ConsoleAnimationDriver(pos);
        this.controls = new ConsoleControlManager(this, animationDriver);
    }

    @Override
    public void registerControllers(AnimatableManager.@NonNull ControllerRegistrar controllerRegistrar) {
        this.idleController = new AnimationController<>("rotor", 15, test -> {
            if (FLIGHT_STATE.equals(this.currentRotorState)) {
                return test.setAndContinue(FLIGHT_ANIM);
            }

            return test.setAndContinue(IDEAL_ANIM);
        });
        this.flightController = this.idleController;

        Map<String, AnimationController<TardisConsoleBlockEntity>> builtControlControllers = new HashMap<>();
        for (String controlId : TardisControlRegistry.allIds()) {
            AnimationController<TardisConsoleBlockEntity> controller = new AnimationController<>(controlId, 0, test ->
                    test.controller().getCurrentRawAnimation() != null ? PlayState.CONTINUE : PlayState.STOP);
            builtControlControllers.put(controlId, controller);
        }
        this.controlControllers = Collections.unmodifiableMap(builtControlControllers);

        controllerRegistrar.add(this.idleController);
        for (AnimationController<TardisConsoleBlockEntity> controller : this.controlControllers.values()) {
            controllerRegistrar.add(controller);
        }
    }


    public void triggerRotorAnimation(String animationName) {
        this.currentRotorState = animationName;
        animationDriver.triggerAnimation("rotor", animationName);
        if (level instanceof ServerLevel serverLevel) {
            Services.NETWORK.broadcastTardisConsoleRotorState(
                    serverLevel.getServer(),
                    new TardisConsoleRotorStatePayload(getBlockPos(), currentRotorState)
            );
        }
    }

    public void applyRotorStateClient(String rotorState) {
        this.currentRotorState = rotorState;
    }

    public void triggerAnimationClient(String animationName, String controllerName) {
        if ("rotor".equals(controllerName)) {
            this.currentRotorState = animationName;
        }

        animationDriver.applyAnimationLocally(controllerName, animationName);
    }

    public boolean getControlState(String controlId) {
        return controls.getControlState(controlId);
    }

    public void triggerControl(String id, boolean activated, @Nullable Player triggeringPlayer) {
        controls.triggerControl(this, id, activated, triggeringPlayer);
    }

    public Entity asEntity() {
        return Entity.of(this, this::sync);
    }

    public List<TardisControlEntity> getControlEntities() {
        return controls.getControlEntities();
    }

    public void addControl(String id, Vec3 offset, float width, float height, float depth) {
        controls.addControl(id, offset, width, height, depth);
    }

    public void respawnControls() {
        controls.respawnControls();
    }

    @SuppressWarnings("unused")
    public void removeControl(String id) {
        controls.removeControl(id);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        controls.discardAll();
    }

    public void sync() {
        if (level != null) {
            setChanged();
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    public void setConsole(TardisConsole console) {
        ConsoleSystem.setConsole(asEntity(), console);
    }

    public void setConsole(TardisConsole console, String variantId) {
        ConsoleSystem.setConsole(asEntity(), console, variantId);
    }

    public TardisConsole getConsole() {
        return ConsoleSystem.consoleOf(asEntity());
    }

    public String getVariant() {
        return ConsoleSystem.variantOf(asEntity());
    }

    public void setVariant(String variantId) {
        ConsoleSystem.setVariant(asEntity(), variantId);
    }

    public @Nullable UUID getTardisId() {
        return getLinkedTardisId().orElse(null);
    }

    @Override
    public @NonNull AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public ComponentStore componentStore() {
        return components;
    }

    @Override
    public Level getHostLevel() {
        return level;
    }

    @Override
    public BlockPos getHostPos() {
        return getBlockPos();
    }

    @Override
    public void markHostChanged() {
        setChanged();
    }

    @Override
    protected void saveAdditional(@NonNull ValueOutput output) {
        super.saveAdditional(output);
        components.save(output, TardisComponentTypes.ALL);
        output.store("Controls", ControlSpec.CODEC.listOf(), controls.getControlSpecs());
        output.store("ControlStates", CompoundTag.CODEC, controls.getControlStates());
    }

    @Override
    public void loadAdditional(@NonNull ValueInput input) {
        super.loadAdditional(input);
        components.load(input, TardisComponentTypes.ALL);
        input.read("Controls", ControlSpec.CODEC.listOf())
                .ifPresent(controls::replaceControlSpecs);
        input.read("ControlStates", CompoundTag.CODEC).ifPresent(controls::mergeControlStates);
    }

    @Override
    public void setLevel(@NonNull Level level) {
        super.setLevel(level);
        getAnimatableInstanceCache().getManagerForId(0);
        animationDriver.bind(() -> this.level, () -> this.currentRotorState, idleController, flightController, controlControllers);
        this.respawnControls();

        if (level.isClientSide() && !humStarted) {
            humStarted = true;
            Constants.LOG.info("TardisConsoleBlockEntity: attempting to start hum at {}", getBlockPos());
            Minecraft.getInstance().execute(() -> {
                Constants.LOG.info("TardisConsoleBlockEntity: executing play() for hum at {}", getBlockPos());
                Minecraft.getInstance().getSoundManager().play(
                        new TardisConsoleHumSoundInstance(getBlockPos(), this::isHumStillValid)
                );
            });
        }
    }

    private boolean isHumStillValid() {
        return !isRemoved() && level != null && level.isClientSide();
    }

    @Override
    public @NonNull CompoundTag getUpdateTag(HolderLookup.@NonNull Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
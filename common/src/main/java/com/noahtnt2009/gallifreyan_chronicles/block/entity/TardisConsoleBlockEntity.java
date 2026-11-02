package com.noahtnt2009.gallifreyan_chronicles.block.entity;

import com.noahtnt2009.gallifreyan_chronicles.tardis.flight.TardisFlightState;
import com.noahtnt2009.gallifreyan_chronicles.tardis.api.Tardis;
import com.noahtnt2009.gallifreyan_chronicles.tardis.api.TardisApi;
import com.noahtnt2009.gallifreyan_chronicles.util.GCScheduler;
import com.noahtnt2009.gallifreyan_chronicles.init.GCTardisControls;
import com.geckolib.animatable.GeoBlockEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.util.GeckoLibUtil;
import com.noahtnt2009.gallifreyan_chronicles.block.ConsoleBlock;
import com.noahtnt2009.gallifreyan_chronicles.client.GCClientHooks;
import com.noahtnt2009.gallifreyan_chronicles.ecs.ComponentHolder;
import com.noahtnt2009.gallifreyan_chronicles.ecs.ComponentStore;
import com.noahtnt2009.gallifreyan_chronicles.ecs.Entity;
import com.noahtnt2009.gallifreyan_chronicles.entity.TardisControlEntity;
import com.noahtnt2009.gallifreyan_chronicles.network.TardisConsoleRotorStatePayload;
import com.noahtnt2009.gallifreyan_chronicles.platform.Services;
import com.noahtnt2009.gallifreyan_chronicles.tardis.console.animation.ConsoleAnimationDriver;
import com.noahtnt2009.gallifreyan_chronicles.tardis.console.animation.ConsoleControllers;
import com.noahtnt2009.gallifreyan_chronicles.client.tardis.ConsoleAnimationCheck;
import com.noahtnt2009.gallifreyan_chronicles.util.FadingValue;
import com.noahtnt2009.gallifreyan_chronicles.tardis.console.control.ConsoleControlHost;
import com.noahtnt2009.gallifreyan_chronicles.tardis.console.control.ConsoleControlManager;
import com.noahtnt2009.gallifreyan_chronicles.tardis.console.TardisConsole;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.ControlSpec;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.TardisControlRegistry;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.TardisComponentTypes;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.TardisLinkable;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.system.ConsoleSyncSystem;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.system.ConsoleSystem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
    private static final int FLIGHT_RECONCILE_DELAY_TICKS = 5;
    private static final String IDLE_STATE = ConsoleControllers.IDLE_STATE;
    private static final String FLIGHT_STATE = ConsoleControllers.FLIGHT_STATE;

    private String currentRotorState = IDLE_STATE;

    private final ConsoleControllers controllers = new ConsoleControllers();
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final ComponentStore components = new ComponentStore();

    private final ConsoleAnimationDriver animationDriver;
    private final ConsoleControlManager controls;
    private boolean clientEffectsStarted = false;

    private boolean controlStatesLoaded = false;
    private @Nullable UUID syncedTardisId;
    private final Map<String, Boolean> clientAppliedStates = new HashMap<>();

    private final FadingValue glowFade = FadingValue.snapping(1.0F);

    public TardisConsoleBlockEntity(BlockPos pos, BlockState state) {
        super(TYPE.get(), pos, state);
        this.animationDriver = new ConsoleAnimationDriver(pos);
        this.controls = new ConsoleControlManager(this, animationDriver);
    }
    @Override
    public void registerControllers(AnimatableManager.@NonNull ControllerRegistrar controllerRegistrar) {
        controllers.register(controllerRegistrar, this::hasPower, () -> this.currentRotorState);
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

    private void settleControlAnimations() {
        if (!controlStatesLoaded) return;
        if (level == null || !level.isClientSide()) return;

        for (String controlId : TardisControlRegistry.allIds()) {
            boolean state = controls.getControlState(controlId);
            Boolean applied = clientAppliedStates.get(controlId);
            if (applied != null && applied == state) continue;
            clientAppliedStates.put(controlId, state);
            if (applied == null && !state && !TardisControlRegistry.get(controlId).snapsWhenInactive()) continue;

            if (!isControlAnimated(controlId)) continue;
            String animation = TardisControlRegistry.get(controlId).getAnimation(state);
            if (animation == null || !hasAnimation(controlId, animation)) continue;

            controllers.requestSnap(controlId);
            animationDriver.applyAnimationLocally(controlId, animation);
        }
    }

    public void applyRotorStateClient(String rotorState) {
        this.currentRotorState = rotorState;
    }

    private boolean hasAnimation(String controlId, String animation) {
        TardisConsole model = getConsole();
        return model == null || ConsoleAnimationCheck.exists(model.id(), controlId, animation);
    }

    public void triggerAnimationClient(String animationName, String controllerName) {
        if (!"rotor".equals(controllerName) && !hasAnimation(controllerName, animationName)) return;
        if ("rotor".equals(controllerName)) {
            this.currentRotorState = animationName;
        } else {
            var control = TardisControlRegistry.get(controllerName);
            if (animationName.equals(control.getAnimation(true))) {
                clientAppliedStates.put(controllerName, true);
            } else if (animationName.equals(control.getAnimation(false))) {
                clientAppliedStates.put(controllerName, false);
            }
        }

        animationDriver.applyAnimationLocally(controllerName, animationName);
    }

    public boolean getControlState(String controlId) {
        return controls.getControlState(controlId);
    }

    public float glowLevel() {
        return glowFade.update(hasPower() ? 1.0F : 0.0F);
    }

    @Override
    public boolean isControlAnimated(String controlId) {
        TardisConsole.ControlEntry entry = findControlEntry(controlId);
        return entry == null || entry.animated();
    }

    private TardisConsole.@Nullable ControlEntry findControlEntry(String controlId) {
        TardisConsole model = getConsole();
        return model == null ? null : model.controlEntry(controlId);
    }

    public int controlCooldownTicks(String controlId) {
        TardisConsole.ControlEntry entry = findControlEntry(controlId);
        float seconds = entry == null ? TardisConsole.ControlEntry.DEFAULT_COOLDOWN_SECONDS : entry.cooldown();
        return Math.round(seconds * 20.0F);
    }

    public boolean isPowerKnown() {
        return controlStatesLoaded;
    }

    public boolean hasPower() {
        if (level != null && level.isClientSide()) {
            return !Boolean.FALSE.equals(clientAppliedStates.get(GCTardisControls.POWER_LEVER));
        }
        return controls.getControlState(GCTardisControls.POWER_LEVER);
    }

    public void triggerControl(String id, boolean activated, @Nullable Player triggeringPlayer) {
        controls.triggerControl(this, id, activated, triggeringPlayer);
        ConsoleSyncSystem.mirrorActivated(this, id, activated);
    }

    public void setControlActivated(String id, boolean activated) {
        setControlActivatedLocal(id, activated);
        ConsoleSyncSystem.mirrorActivated(this, id, activated);
    }

    public void setControlActivatedLocal(String id, boolean activated) {
        if (controls.getControlState(id) == activated) return;
        controls.setControlState(id, activated);
        controls.playControlAnimation(id, activated, null);
    }

    public void reconcileControl(String id, boolean activated) {
        reconcileControlLocal(id, activated);
        ConsoleSyncSystem.mirrorReconcile(this, id, activated);
    }

    public void reconcileControlLocal(String id, boolean activated) {
        if (controls.getControlState(id) == activated) return;
        controls.setControlState(id, activated);
        sync();
    }

    public @Nullable UUID syncedTardisId() {
        return syncedTardisId;
    }

    public void setSyncedTardisId(@Nullable UUID id) {
        this.syncedTardisId = id;
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
        ConsoleSyncSystem.unregister(this);
    }

    public CompoundTag copyControlStates() {
        return controls.getControlStates().copy();
    }

    public void applyCarriedControlStates(CompoundTag states) {
        controls.mergeControlStates(states);
        this.currentRotorState = controls.getControlState("flight_lever") ? FLIGHT_STATE : IDLE_STATE;
        sync();
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
    public Direction getHostFacing() {
        BlockState state = getBlockState();
        return state.hasProperty(ConsoleBlock.FACING)
                ? state.getValue(ConsoleBlock.FACING)
                : Direction.NORTH;
    }

    @Override
    public boolean isHostUpsideDown() {
        return ConsoleBlock.isUpsideDown(getBlockState());
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
        input.read("ControlStates", CompoundTag.CODEC).ifPresent(tag -> {
            controls.mergeControlStates(tag);
            controlStatesLoaded = true;
        });
        this.currentRotorState = controls.getControlState("flight_lever") ? FLIGHT_STATE : IDLE_STATE;
        settleControlAnimations();
    }

    @Override
    public void setLevel(@NonNull Level level) {
        super.setLevel(level);
        if (level instanceof ServerLevel) {
            ConsoleSyncSystem.register(this);
        }
        getAnimatableInstanceCache().getManagerForId(0);
        animationDriver.bind(() -> this.level, () -> this.currentRotorState, controllers.rotor(), controllers.rotor(), controllers.controls());
        this.respawnControls();
        settleControlAnimations();

        if (level instanceof ServerLevel serverLevel) {
            GCScheduler.runLater(serverLevel.getServer(), FLIGHT_RECONCILE_DELAY_TICKS, this::reconcileFlightWithExterior);
        }

        if (level.isClientSide() && !clientEffectsStarted) {
            clientEffectsStarted = true;
            GCClientHooks.startConsoleClientEffects(this);
        }
    }

    private void reconcileFlightWithExterior() {
        if (isRemoved() || !(level instanceof ServerLevel)) return;
        TardisFlightState state = TardisApi.of(this).map(Tardis::flightState).orElse(TardisFlightState.IDLE);
        boolean inVortex = state == TardisFlightState.TAKING_OFF || state == TardisFlightState.IN_VORTEX;
        if (!inVortex || controls.getControlState(GCTardisControls.FLIGHT_LEVER)) return;
        reconcileControl(GCTardisControls.FLIGHT_LEVER, true);
        triggerRotorAnimation(FLIGHT_STATE);
    }

    public boolean isInFlight() {
        return FLIGHT_STATE.equals(this.currentRotorState);
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
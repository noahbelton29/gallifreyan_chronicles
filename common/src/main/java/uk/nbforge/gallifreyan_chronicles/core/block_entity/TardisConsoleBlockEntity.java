package uk.nbforge.gallifreyan_chronicles.core.block_entity;

import com.geckolib.animatable.GeoBlockEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.util.GeckoLibUtil;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
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
import uk.nbforge.gallifreyan_chronicles.client.GCClientHooks;
import uk.nbforge.gallifreyan_chronicles.client.tardis.ConsoleAnimationCheck;
import uk.nbforge.gallifreyan_chronicles.codec.UuidCodec;
import uk.nbforge.gallifreyan_chronicles.core.GCTardisControls;
import uk.nbforge.gallifreyan_chronicles.core.block.ConsoleBlock;
import uk.nbforge.gallifreyan_chronicles.core.entity.TardisControlEntity;
import uk.nbforge.gallifreyan_chronicles.network.tardis.TardisConsoleRotorStatePayload;
import uk.nbforge.gallifreyan_chronicles.platform.Services;
import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import uk.nbforge.gallifreyan_chronicles.tardis.TardisDimensionUtil;
import uk.nbforge.gallifreyan_chronicles.tardis.TardisLinkable;
import uk.nbforge.gallifreyan_chronicles.tardis.console.ConsoleSelection;
import uk.nbforge.gallifreyan_chronicles.tardis.console.ConsoleSync;
import uk.nbforge.gallifreyan_chronicles.tardis.console.TardisConsole;
import uk.nbforge.gallifreyan_chronicles.tardis.console.animation.ConsoleAnimationDriver;
import uk.nbforge.gallifreyan_chronicles.tardis.console.animation.ConsoleControllers;
import uk.nbforge.gallifreyan_chronicles.tardis.control.ConsoleControlHost;
import uk.nbforge.gallifreyan_chronicles.tardis.control.ConsoleControlManager;
import uk.nbforge.gallifreyan_chronicles.tardis.control.ControlSpec;
import uk.nbforge.gallifreyan_chronicles.tardis.control.TardisControl;
import uk.nbforge.gallifreyan_chronicles.tardis.control.TardisControls;
import uk.nbforge.gallifreyan_chronicles.tardis.flight.TardisFlightState;
import uk.nbforge.gallifreyan_chronicles.util.BlockEntitySyncUtil;
import uk.nbforge.gallifreyan_chronicles.util.FadingValue;
import uk.nbforge.gallifreyan_chronicles.util.SavedValue;
import uk.nbforge.gallifreyan_chronicles.util.SchedulerUtil;

public class TardisConsoleBlockEntity extends BlockEntity
        implements GeoBlockEntity, TardisLinkable, ConsoleControlHost {
    public static Supplier<BlockEntityType<TardisConsoleBlockEntity>> TYPE;

    private static final int FLIGHT_RECONCILE_DELAY_TICKS = 5;
    private static final String IDLE_STATE = ConsoleControllers.IDLE_STATE;
    private static final String FLIGHT_STATE = ConsoleControllers.FLIGHT_STATE;

    private final SavedValue<ConsoleSelection> selection =
            new SavedValue<>("console", ConsoleSelection.CODEC, ConsoleSelection::ofDefault);
    private final SavedValue<UUID> tardisLink = new SavedValue<>("tardis_link", UuidCodec.CODEC);
    private @Nullable UUID syncedTardisId;
    private String currentRotorState = IDLE_STATE;
    private boolean controlStatesLoaded = false;

    // client only, what each control's last played animation shows, see getControlState
    private final Map<String, Boolean> clientAppliedStates = new HashMap<>();
    private final FadingValue glowFade = FadingValue.snapping(1.0f);
    private boolean clientEffectsStarted = false;

    private final ConsoleAnimationDriver animationDriver;
    private final ConsoleControlManager controls;
    private final ConsoleControllers controllers = new ConsoleControllers();
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public TardisConsoleBlockEntity(BlockPos pos, BlockState state) {
        super(TYPE.get(), pos, state);
        this.animationDriver = new ConsoleAnimationDriver(pos);
        this.controls = new ConsoleControlManager(this, animationDriver);
    }

    public TardisConsole getConsole() {
        return selection.get().console();
    }

    public void setConsole(TardisConsole console) {
        selection.set(ConsoleSelection.of(console));
        sync();
    }

    public void setConsole(TardisConsole console, String variantId) {
        selection.set(ConsoleSelection.of(console, variantId));
        sync();
    }

    public String getVariant() {
        return selection.get().variantId();
    }

    public void setVariant(String variantId) {
        selection.set(selection.get().withVariant(variantId));
        sync();
    }

    public @Nullable UUID getTardisId() {
        return getLinkedTardisId().orElse(null);
    }

    public List<TardisConsoleBlockEntity> linkedConsoles() {
        return ConsoleSync.liveConsolesWith(this);
    }

    @Override
    public Optional<UUID> getLinkedTardisId() {
        return Optional.ofNullable(tardisLink.getOrNull());
    }

    @Override
    public void linkToTardis(UUID tardisId) {
        tardisLink.set(tardisId);
        sync();
    }

    @Override
    public void unlinkFromTardis() {
        tardisLink.set(null);
        sync();
    }

    public @Nullable UUID syncedTardisId() {
        return syncedTardisId;
    }

    public void setSyncedTardisId(@Nullable UUID id) {
        this.syncedTardisId = id;
    }

    // outside an interior it's just decoration, controls animate but no lights or hum
    public boolean isInTardis() {
        return TardisDimensionUtil.isInterior(level);
    }

    public boolean isPowerKnown() {
        return controlStatesLoaded;
    }

    public boolean hasPower() {
        if (level != null && level.isClientSide()) {
            return !Boolean.FALSE.equals(clientAppliedStates.get(GCTardisControls.POWER_CONTROL));
        }
        return controls.getControlState(GCTardisControls.POWER_CONTROL);
    }

    public float glowLevel() {
        return glowFade.update(hasPower() && isInTardis() ? 1.0f : 0.0f);
    }

    // the client uses the last animation it played, the real state is only as fresh as the last full resync
    public boolean getControlState(String controlId) {
        if (level != null && level.isClientSide()) {
            Boolean applied = clientAppliedStates.get(controlId);
            if (applied != null) return applied;
        }
        return controls.getControlState(controlId);
    }

    public void triggerControl(String id, boolean activated, @Nullable Player triggeringPlayer) {
        controls.triggerControl(this, id, activated, triggeringPlayer);
        ConsoleSync.mirrorActivated(this, id, activated);
    }

    public void setControlActivated(String id, boolean activated) {
        setControlActivatedLocal(id, activated);
        ConsoleSync.mirrorActivated(this, id, activated);
    }

    public void setControlActivatedLocal(String id, boolean activated) {
        if (controls.getControlState(id) == activated) return;
        controls.setControlState(id, activated);
        controls.playControlAnimation(id, activated, null);
    }

    public void reconcileControl(String id, boolean activated) {
        reconcileControlLocal(id, activated);
        ConsoleSync.mirrorReconcile(this, id, activated);
    }

    public void reconcileControlLocal(String id, boolean activated) {
        if (controls.getControlState(id) == activated) return;
        controls.setControlState(id, activated);
        sync();
    }

    public void playControlAnimationOnly(String id, boolean activated) {
        controls.playControlAnimation(id, activated, null);
    }

    public void playControlAnimationReversed(String id, @Nullable Player triggeringPlayer) {
        controls.playControlAnimationReversed(id, triggeringPlayer);
    }

    public void animateControlOnly(String id, boolean activated, @Nullable Player triggeringPlayer) {
        controls.setControlState(id, activated);
        controls.playControlAnimation(id, activated, triggeringPlayer);
    }

    public CompoundTag copyControlStates() {
        return controls.getControlStates().copy();
    }

    public void applyCarriedControlStates(CompoundTag states) {
        controls.mergeControlStates(states);
        this.currentRotorState = controls.getControlState("flight_lever") ? FLIGHT_STATE : IDLE_STATE;
        sync();
    }

    public int controlCooldownTicks(String controlId) {
        TardisConsole.ControlEntry entry = findControlEntry(controlId);
        float seconds = entry == null ? TardisConsole.ControlEntry.DEFAULT_COOLDOWN_SECONDS : entry.cooldown();
        return Math.round(seconds * 20.0f);
    }

    @Override
    public @Nullable String resolveControlAnimation(String controlId, boolean activated) {
        TardisControl control = TardisControls.get(controlId);
        String fallback = control.getAnimation(activated);
        TardisConsole.ControlEntry entry = findControlEntry(controlId);
        return entry == null ? fallback : entry.animation().animationFor(activated, fallback);
    }

    @Override
    public boolean controlSnapsWhenInactive(String controlId) {
        boolean fallback = TardisControls.get(controlId).snapsWhenInactive();
        TardisConsole.ControlEntry entry = findControlEntry(controlId);
        return entry == null ? fallback : entry.animation().snapsWhenInactive(fallback);
    }

    private TardisConsole.@Nullable ControlEntry findControlEntry(String controlId) {
        TardisConsole model = getConsole();
        return model == null ? null : model.controlEntry(controlId);
    }

    public List<TardisControlEntity> getControlEntities() {
        return controls.getControlEntities();
    }

    public void addControl(String id, Vec3 offset, float width, float height, float depth) {
        controls.addControl(id, offset, width, height, depth);
    }

    @SuppressWarnings("unused")
    public void removeControl(String id) {
        controls.removeControl(id);
    }

    public void respawnControls() {
        controls.respawnControls();
    }

    public void ensureControls() {
        controls.ensureControls();
    }

    public void triggerAnimationClient(String animationName, String controllerName) {
        if (!"rotor".equals(controllerName) && lacksAnimation(controllerName, animationName)) return;
        if ("rotor".equals(controllerName)) {
            this.currentRotorState = animationName;
        } else {
            if (animationName.equals(resolveControlAnimation(controllerName, true))) {
                clientAppliedStates.put(controllerName, true);
            } else if (animationName.equals(resolveControlAnimation(controllerName, false))) {
                clientAppliedStates.put(controllerName, false);
            }
        }

        animationDriver.applyAnimationLocally(controllerName, animationName);
    }

    private void settleControlAnimations() {
        if (!controlStatesLoaded) return;
        if (level == null || !level.isClientSide()) return;

        for (String controlId : TardisControls.ids()) {
            boolean state = controls.getControlState(controlId);
            Boolean applied = clientAppliedStates.get(controlId);
            if (applied != null && applied == state) continue;
            clientAppliedStates.put(controlId, state);
            if (applied == null && !state && !controlSnapsWhenInactive(controlId)) continue;

            String animation = resolveControlAnimation(controlId, state);
            if (animation == null || lacksAnimation(controlId, animation)) continue;

            controllers.requestSnap(controlId);
            animationDriver.applyAnimationLocally(controlId, animation);
        }
    }

    // no model loaded yet counts as yes
    private boolean lacksAnimation(String controlId, String animation) {
        TardisConsole model = getConsole();
        return model != null && ConsoleAnimationCheck.missing(model.id(), controlId, animation);
    }

    public boolean isInFlight() {
        return FLIGHT_STATE.equals(this.currentRotorState);
    }

    public void triggerRotorAnimation(String animationName) {
        this.currentRotorState = animationName;
        animationDriver.triggerAnimation("rotor", animationName);
        if (level instanceof ServerLevel serverLevel) {
            Services.NETWORK.broadcast(
                    serverLevel.getServer(),
                    new TardisConsoleRotorStatePayload(
                            serverLevel.dimension().identifier(), getBlockPos(), currentRotorState));
        }
    }

    public void applyRotorStateClient(String rotorState) {
        this.currentRotorState = rotorState;
    }

    private void reconcileFlightWithExterior() {
        if (isRemoved() || !(level instanceof ServerLevel)) return;
        TardisFlightState state = Tardis.of(this).map(Tardis::flightState).orElse(TardisFlightState.IDLE);
        boolean inVortex = state == TardisFlightState.TAKING_OFF || state == TardisFlightState.IN_VORTEX;
        if (!inVortex || controls.getControlState(GCTardisControls.SPACE_TIME_THROTTLE)) return;
        reconcileControl(GCTardisControls.SPACE_TIME_THROTTLE, true);
        triggerRotorAnimation(FLIGHT_STATE);
    }

    @Override
    public void setLevel(@NonNull Level level) {
        super.setLevel(level);
        if (level instanceof ServerLevel) {
            ConsoleSync.register(this);
        }
        getAnimatableInstanceCache().getManagerForId(0);
        animationDriver.bind(
                () -> this.level,
                () -> this.currentRotorState,
                controllers.rotor(),
                controllers.rotor(),
                controllers.controls());
        this.respawnControls();
        settleControlAnimations();

        if (level instanceof ServerLevel serverLevel) {
            SchedulerUtil.runLater(
                    serverLevel.getServer(), FLIGHT_RECONCILE_DELAY_TICKS, this::reconcileFlightWithExterior);
        }

        if (level.isClientSide() && !clientEffectsStarted) {
            clientEffectsStarted = true;
            GCClientHooks.startConsoleClientEffects(this);
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        controls.discardAll();
        ConsoleSync.unregister(this);
    }

    public void sync() {
        BlockEntitySyncUtil.sendToClients(this);
    }

    @Override
    protected void saveAdditional(@NonNull ValueOutput output) {
        super.saveAdditional(output);
        selection.save(output);
        tardisLink.save(output);
        output.store("Controls", ControlSpec.CODEC.listOf(), controls.getControlSpecs());
        output.store("ControlStates", CompoundTag.CODEC, controls.getControlStates());
    }

    @Override
    public void loadAdditional(@NonNull ValueInput input) {
        super.loadAdditional(input);
        selection.load(input);
        tardisLink.load(input);
        input.read("Controls", ControlSpec.CODEC.listOf()).ifPresent(controls::replaceControlSpecs);
        input.read("ControlStates", CompoundTag.CODEC).ifPresent(tag -> {
            controls.mergeControlStates(tag);
            controlStatesLoaded = true;
        });
        this.currentRotorState = controls.getControlState("flight_lever") ? FLIGHT_STATE : IDLE_STATE;
        settleControlAnimations();
    }

    @Override
    public @NonNull CompoundTag getUpdateTag(HolderLookup.@NonNull Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void registerControllers(AnimatableManager.@NonNull ControllerRegistrar controllerRegistrar) {
        controllers.register(controllerRegistrar, () -> hasPower() && isInTardis(), () -> this.currentRotorState);
    }

    @Override
    public @NonNull AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
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
    public Direction getHostFacing() {
        BlockState state = getBlockState();
        return state.hasProperty(ConsoleBlock.FACING) ? state.getValue(ConsoleBlock.FACING) : Direction.NORTH;
    }

    @Override
    public boolean isHostUpsideDown() {
        return ConsoleBlock.isUpsideDown(getBlockState());
    }

    @Override
    public void markHostChanged() {
        setChanged();
    }
}

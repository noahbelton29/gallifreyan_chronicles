package uk.nbforge.gallifreyan_chronicles.core.block_entities;

import uk.nbforge.gallifreyan_chronicles.tardis.exterior.fade.PulseFadeType;
import uk.nbforge.gallifreyan_chronicles.util.MessageUtil;
import uk.nbforge.gallifreyan_chronicles.util.BlockEntitySyncUtil;

import uk.nbforge.gallifreyan_chronicles.util.GameClockUtil;
import uk.nbforge.gallifreyan_chronicles.tardis.flight.RematCatch;
import uk.nbforge.gallifreyan_chronicles.client.GCClientHooks;
import uk.nbforge.gallifreyan_chronicles.core.block.TardisTopBlock;
import uk.nbforge.gallifreyan_chronicles.core.dynamic_registries.TardisDoorRegistry;
import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import uk.nbforge.gallifreyan_chronicles.tardis.TardisApi;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.mood.TardisMoods;
import com.geckolib.animatable.GeoBlockEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.util.GeckoLibUtil;
import uk.nbforge.gallifreyan_chronicles.core.block.TardisBlock;
import uk.nbforge.gallifreyan_chronicles.client.renderer.geo_layer.TardisExteriorKeyLayer;
import uk.nbforge.gallifreyan_chronicles.ecs.ComponentHolder;
import uk.nbforge.gallifreyan_chronicles.ecs.ComponentType;
import uk.nbforge.gallifreyan_chronicles.ecs.ComponentStore;
import uk.nbforge.gallifreyan_chronicles.ecs.Entity;
import uk.nbforge.gallifreyan_chronicles.core.entity.TardisKeyEntity;
import uk.nbforge.gallifreyan_chronicles.core.GCDataComponents;
import uk.nbforge.gallifreyan_chronicles.tardis.ecs.TardisLinkable;
import uk.nbforge.gallifreyan_chronicles.tardis.ecs.component.DoorState;
import uk.nbforge.gallifreyan_chronicles.tardis.flight.TardisFlightState;
import uk.nbforge.gallifreyan_chronicles.tardis.ecs.TardisComponentTypes;
import uk.nbforge.gallifreyan_chronicles.tardis.ecs.component.TardisSubsystem;
import uk.nbforge.gallifreyan_chronicles.tardis.ecs.component.TransformComponent;
import uk.nbforge.gallifreyan_chronicles.tardis.ecs.system.DoorSyncSystem;
import uk.nbforge.gallifreyan_chronicles.tardis.ecs.system.DoorSystem;
import uk.nbforge.gallifreyan_chronicles.tardis.ecs.system.ExteriorSystem;
import uk.nbforge.gallifreyan_chronicles.tardis.ecs.system.GlowSystem;
import uk.nbforge.gallifreyan_chronicles.tardis.ecs.system.SubsystemSystem;
import uk.nbforge.gallifreyan_chronicles.tardis.ecs.system.TardisInteriorSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import uk.nbforge.gallifreyan_chronicles.util.GCSchedulerUtil;
import net.minecraft.util.Prediction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.nbforge.gallifreyan_chronicles.client.renderer.util.CloakKeyReveal;
import uk.nbforge.gallifreyan_chronicles.util.FadingValue;
import uk.nbforge.gallifreyan_chronicles.tardis.exterior.fade.CloakFade;
import uk.nbforge.gallifreyan_chronicles.tardis.exterior.fade.FadeType;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import uk.nbforge.gallifreyan_chronicles.tardis.exterior.effect.ExteriorSounds;
import uk.nbforge.gallifreyan_chronicles.tardis.exterior.effect.TakeoffWind;
import uk.nbforge.gallifreyan_chronicles.tardis.exterior.effect.LockDamageEffects;
import uk.nbforge.gallifreyan_chronicles.tardis.key.HeldKey;
import uk.nbforge.gallifreyan_chronicles.tardis.key.TardisKeyLocator;
import uk.nbforge.gallifreyan_chronicles.tardis.flight.ExteriorFlight;
import uk.nbforge.gallifreyan_chronicles.tardis.TardisExterior;

public class TardisExteriorBlockEntity extends BlockEntity implements GeoBlockEntity, ComponentHolder, TardisLinkable,
        TardisExteriorKeyLayer.TardisExteriorRenderable {
    public static Supplier<BlockEntityType<TardisExteriorBlockEntity>> TYPE;
    private static final long KEY_JAM_COOLDOWN_TICKS = 40L;
    private static final long KEY_TURN_COOLDOWN_TICKS = 20L;
    private long lastLockedFeedbackAt = 0L;
    private long keyCooldownUntil = 0L;
    private AnimationController<TardisExteriorBlockEntity> controller;
    private boolean locked = false;
    private static final int POWER_CHECK_INTERVAL_TICKS = 10;
    private static final int ENTRY_ZONE_INTERVAL_TICKS = 2;
    private boolean powered = true;
    private boolean portalActive = false;
    private int doorAnimSeq = 0;
    private String doorAnim = "";
    private boolean doorAnimSeqKnown = false;
    private boolean doorAnimInstant = false;
    private int snapFramesLeft = 0;
    /* a snap runs the animation at this speed for a few frames, then goes back to normal */
    private static final int SNAP_SPEED = 999999;
    private static final int SNAP_FRAMES = 3;
    private final int tickOffset;
    private final ExteriorFlight flight = new ExteriorFlight();
    private boolean lastShadowActive = true;
    private final HeldKey heldKey = new HeldKey();
    private final CloakFade cloak = new CloakFade();
    private static final float KEY_REVEAL_OPACITY = 0.2f;
    private static final int TRAVEL_RESYNC_TICKS = 20;
    private final FadingValue keyReveal = new FadingValue(0.5f, 0.0f);
    private final ComponentStore components = new ComponentStore();
    private final Entity ecsEntity = Entity.of(this, this::sync);
    private AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private boolean needsStateSync = false;
    private TardisKeyEntity keyEntity = null;
    private volatile Vec3 cachedKeyBoneWorldPos = null;
    private static final long WRONG_KEY_LOCKOUT_TICKS = 20L * 60L;
    private static final int WRONG_KEY_JAM_DELAY_TICKS = 10;
    private static final int WRONG_KEY_RETURN_DELAY_TICKS = 30;
    private static final UUID UNLINKED_KEY = new UUID(0L, 0L);
    private final Map<UUID, Long> wrongKeyLockouts = new HashMap<>();

    public void setCachedKeyBoneWorldPos(Vec3 pos) {
        this.cachedKeyBoneWorldPos = pos;
    }

    @SuppressWarnings("unused")
    public @Nullable Vec3 getCachedKeyBoneWorldPos() {
        return cachedKeyBoneWorldPos;
    }

    public void spawnKeyEntity() {
        if (level == null || level.isClientSide() || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (keyEntity != null && keyEntity.isAlive()) {
            keyEntity.discard();
        }
        TardisKeyEntity entity =
                TardisKeyEntity.create(serverLevel, getBlockPos(), getKeyWorldPos());
        serverLevel.addFreshEntity(entity);
        this.keyEntity = entity;
    }

    public Vec3 getKeyWorldPos() {
        return Vec3.atCenterOf(getBlockPos()).add(getExterior().worldKeyOffset(getYaw()));
    }

    public void discardKeyEntity() {
        if (keyEntity != null) {
            keyEntity.discard();
            keyEntity = null;
        }
    }

    public TardisExteriorBlockEntity(BlockPos pos, BlockState state) {
        super(TYPE.get(), pos, state);
        this.tickOffset = Math.floorMod(pos.hashCode(), POWER_CHECK_INTERVAL_TICKS);
    }

    public void insertKey(Player player, InteractionHand hand, ItemStack held) {
        ItemStack toStore = held.copyWithCount(1);
        toStore.set(GCDataComponents.KEY_INSERTED, true);
        setHeldKey(player.getUUID(), toStore);

        ItemStack remaining = held.copyWithCount(held.getCount() - 1);
        player.setItemInHand(hand, remaining.isEmpty() ? ItemStack.EMPTY : remaining);
    }

    public boolean canRemoveKey(Player player) {
        return player.getUUID().equals(heldKey.owner())
                || TardisApi.of(this).map(tardis -> tardis.isOwner(player)).orElse(false);
    }

    public boolean hasKeyRendered() {
        return heldKey.isPresent();
    }

    public boolean isPowered() {
        return powered;
    }

    public boolean areDoorsFullyClosed() {
        return getDoorState().isClosed()
                && !DoorSystem.isAnimating(asEntity(), System.currentTimeMillis());
    }

    public TardisFlightState getFlightState() {
        if (level != null && !level.isClientSide() && flight.expireState(GameClockUtil.now(level))) {
            setChanged();
        }
        return flight.state();
    }

    public void setFlightState(TardisFlightState state, long durationTicks) {
        if (level == null || level.isClientSide()) return;
        flight.setState(state, GameClockUtil.now(level), durationTicks);
        setChanged();
    }

    public boolean isTravelling() {
        return !getFlightState().isIdle();
    }

    public boolean areDoorsSealedForTravel() {
        return isTravelling() && getDoorState().isClosed();
    }

    public boolean isLocked() {
        return locked;
    }

    public void toggleKeyLockAnimation(@Nullable Player player) {
        if (!hasKeyRendered()) {
            return;
        }
        if (level != null && !level.isClientSide()) {
            long now = GameClockUtil.now(level);
            if (now < keyCooldownUntil) {
                return;
            }
            if (SubsystemSystem.rollJam(asEntity(), TardisSubsystem.DOOR_LOCK, level.getRandom())) {
                keyCooldownUntil = now + KEY_JAM_COOLDOWN_TICKS;
                announceKeyJammed(player);
                return;
            }
            keyCooldownUntil = now + KEY_TURN_COOLDOWN_TICKS;
        }
        boolean nowLocked = !locked;
        String animName = nowLocked ? "key_lock" : "key_unlock";
        locked = nowLocked;
        triggerAnim("key_controller", animName);
        ExteriorSounds.keyTurn(level, getBlockPos(), locked);
        sync();

        if (level instanceof ServerLevel serverLevel) {
            UUID id = getTardisId();
            if (id != null) {
                DoorSyncSystem.onLockChanged(serverLevel.getServer(), id, nowLocked, false);
            }
        }
    }

    public void setLockedFromConsole(boolean nowLocked) {
        if (locked == nowLocked) return;
        locked = nowLocked;
        ExteriorSounds.keyTurn(level, getBlockPos(), locked);
        sync();

        if (level instanceof ServerLevel serverLevel) {
            UUID id = getTardisId();
            if (id != null) {
                DoorSyncSystem.onLockChanged(serverLevel.getServer(), id, nowLocked, true);
            }
        }
    }

    public @Nullable UUID getRenderedKeyOwner() {
        return heldKey.owner();
    }

    @Override
    public @Nullable ItemStack gc$getHeldKeyStack() {
        return heldKey.stack();
    }

    public void setHeldKey(UUID owner, ItemStack stack) {
        heldKey.set(owner, stack);
        sync();
    }

    public ItemStack clearHeldKey() {
        ItemStack returned = heldKey.clear();
        sync();
        return returned;
    }

    @Override
    public ComponentStore componentStore() {
        return components;
    }

    public Entity asEntity() {
        return ecsEntity;
    }

    @Override
    public void setLevel(@NonNull Level level) {
        super.setLevel(level);
        if (cache == null) {
            cache = GeckoLibUtil.createInstanceCache(this);
        }
        if (level.isClientSide()) {
            needsStateSync = true;
        }
    }

    private String getAnimationForDoorState(DoorState state) {
        return switch (state) {
            case CLOSED -> "close_all";
            case LEFT_OPEN -> "open_left";
            case RIGHT_OPEN -> "open_right";
            case BOTH_OPEN -> "open_all";
        };
    }

    public TardisExterior getExterior() {
        return ExteriorSystem.exteriorOf(asEntity());
    }

    public void setExterior(TardisExterior exterior) {
        ExteriorSystem.setExterior(asEntity(), exterior);
        resetAnimationState();
        UUID id = getTardisId();
        if (id == null || level == null || level.isClientSide()) return;
        if (level.getServer() != null) {
            TardisKeyLocator.refresh(level.getServer(), id);
        }
        String doorVariant = TardisDoorRegistry.interiorVariantForExterior(exterior.id());
        if (doorVariant == null) return;
        for (InteriorDoorBlockEntity door : DoorSyncSystem.doorsOf(id)) {
            if (door.isLinked()) door.setDoorVariant(doorVariant);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void copyStateFrom(TardisExteriorBlockEntity other) {
        for (ComponentType<?> type : TardisComponentTypes.ALL) {
            uk.nbforge.gallifreyan_chronicles.ecs.Component value = other.components.getOrNull((ComponentType) type);
            if (value != null) {
                components.set((ComponentType) type, value);
            }
        }
        this.locked = other.locked;
        this.flight.copyFrom(other.flight);
        this.cloak.copyFrom(other.cloak);
        this.heldKey.copyFrom(other.heldKey);
        resetAnimationState();
    }

    private void snapToState() {
        if (level == null || !level.isClientSide() || controller == null) return;
        DoorState state = getDoorState();
        String animName = getAnimationForDoorState(state);
        snapAnimation(controller, animName);
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

    /* the doors go to closed right away on every client, no closing animation (used when the TARDIS is spawned) */
    public void snapDoorsShut() {
        if (level == null || level.isClientSide()) return;
        DoorSystem.snapToState(asEntity(), DoorState.CLOSED);
        doorAnimSeq++;
        doorAnim = getAnimationForDoorState(DoorState.CLOSED);
        doorAnimInstant = true;
        sync();
    }

    public void resetAnimationState() {
        needsStateSync = true;
        if (level != null && level.isClientSide()) {
            snapToState();
        } else {
            reapplyDoorVisualState();
        }
        sync();
    }

    private void reapplyDoorVisualState() {
        if (level != null && level.isClientSide()) return;
        DoorState state = getDoorState();
        doorAnimSeq++;
        doorAnim = getAnimationForDoorState(state);
        doorAnimInstant = true;
        sync();
    }

    public float getYaw() {
        return asEntity().get(TardisComponentTypes.TRANSFORM).yaw();
    }

    public void setYaw(float yaw) {
        asEntity().set(TardisComponentTypes.TRANSFORM, new TransformComponent(yaw));
    }

    public void setInFlight(boolean inFlight) {
        long now = level != null ? GameClockUtil.now(level) : 0L;

        this.flight.setInFlight(inFlight, now);

        if (level != null && !level.isClientSide()) {
            BlockState state = getBlockState();

            if (state.hasProperty(TardisBlock.IN_FLIGHT)
                    && state.getValue(TardisBlock.IN_FLIGHT) != inFlight) {
                level.setBlock(
                        worldPosition,
                        state.setValue(TardisBlock.IN_FLIGHT, inFlight),
                        Block.UPDATE_ALL
                );
            }
        }

        setChanged();
    }

    private void setInFlight(boolean inFlight, long atTick) {
        if (level == null || level.isClientSide()) return;
        if (flight.setInFlight(inFlight, atTick)) {
            sync();
            /* landing on top of someone pulls them inside */
            if (!inFlight) RematCatch.onRemat(this, atTick);
        }
    }

    public void vanishInstantly() {
        if (level == null || level.isClientSide()) return;
        flight.vanish(GameClockUtil.now(level));
        BlockState state = getBlockState();
        if (state.hasProperty(TardisBlock.IN_FLIGHT) && !state.getValue(TardisBlock.IN_FLIGHT)) {
            level.setBlock(worldPosition, state.setValue(TardisBlock.IN_FLIGHT, true), Block.UPDATE_ALL);
        }
        sync();
    }

    public void scheduleLanding(int delayTicks) {
        if (level == null || level.isClientSide()) return;
        if (flight.scheduleLanding(GameClockUtil.now(level), delayTicks)) setChanged();
    }

    public void abortTakeoff(int fadeInTicks) {
        if (level == null || level.isClientSide()) return;
        flight.cancelLanding();
        flight.setQuickFadeIn(fadeInTicks);
        setInFlight(false);
        setFlightState(TardisFlightState.LANDING, fadeInTicks);
        sync();
    }

    public void cancelLanding() {
        if (flight.cancelLanding()) setChanged();
    }

    private void tickLanding(Level level) {
        long dueTick = flight.consumeDueLanding(GameClockUtil.now(level));
        if (dueTick < 0L) return;
        setInFlight(false, dueTick);
        setChanged();
    }

    public FadeType getFadeType() {
        return flight.fadeType();
    }

    public void setFadeType(FadeType fadeType) {
        boolean serverSide = level != null && !level.isClientSide();
        long now = serverSide ? GameClockUtil.now(level) : 0L;
        if (flight.setFadeType(fadeType, now, serverSide)) sync();
    }

    private void refreshFadeType() {
        if (level != null && flight.refreshFadeType(GameClockUtil.now(level))) sync();
    }

    public boolean isCloaked() {
        return cloak.isCloaked();
    }

    public void setCloaked(boolean cloaked) {
        if (level == null || level.isClientSide()) return;
        if (cloak.set(cloaked, GameClockUtil.now(level))) sync();
    }

    private float cloakOpacity(float partialTick) {
        if (level == null) return cloak.restingOpacity();
        return cloak.opacity(GameClockUtil.now(level), partialTick);
    }

    public boolean isShadowActive() {
        if (level == null) return true;
        return !flight.inFlight() && flight.opacity(GameClockUtil.now(level), 0.0f) >= 1.0f;
    }

    public boolean isFullyInvisible() {
        if (level == null) return false;
        return flight.isFullyInvisible(GameClockUtil.now(level));
    }

    public float getOpacity(float partialTick) {
        float flight = flightOpacity(partialTick);
        float base = Math.min(flight, cloakOpacity(partialTick));
        if (level == null || !level.isClientSide()) return base;
        float revealed = Math.min(flight, updateKeyReveal() * KEY_REVEAL_OPACITY);
        return Math.max(base, revealed);
    }

    private float updateKeyReveal() {
        boolean target = cloak.isCloaked() && CloakKeyReveal.isHoldingKeyFor(getTardisId());
        return keyReveal.update(target ? 1.0f : 0.0f);
    }

    private float flightOpacity(float partialTick) {
        if (level == null) return flight.restingOpacity();
        return flight.opacity(GameClockUtil.now(level), partialTick);
    }

    public @Nullable UUID getTardisId() {
        return getLinkedTardisId().orElse(null);
    }

    public DoorState getDoorState() {
        return DoorSystem.stateOf(asEntity());
    }

    public boolean isPortalActive() {
        return portalActive;
    }

    public void setPortalActive(boolean active) {
        if (portalActive == active) return;
        portalActive = active;
        sync();
    }

    /* how bright the lamp is right now, 0 to 1. it flickers while taking off and landing:
       classic who snaps on and off, new who fades in and out */
    private static final float LAMP_FLICKER_TICKS = 25f;

    public float lampBrightness(float partialTick) {
        if (!isGlowing()) return 0f;
        if (!isFlickering(partialTick) || level == null) return 1f;
        float cycles = (level.getGameTime() + partialTick) / LAMP_FLICKER_TICKS;
        if (getFadeType() instanceof PulseFadeType) {
            return 0.5f + 0.5f * (float) Math.cos(cycles * Math.PI);
        }
        return ((int) cycles) % 2 == 0 ? 1f : 0f;
    }

    /* taking off or landing and still fading, not fully gone or fully landed yet */
    private boolean isFlickering(float partialTick) {
        if (!getFlightState().isTransitioning()) return false;
        float opacity = getOpacity(partialTick);
        boolean gone = getFlightState() == TardisFlightState.TAKING_OFF && opacity <= 0.001f;
        boolean landed = getFlightState() == TardisFlightState.LANDING && opacity >= 0.999f;
        return !gone && !landed;
    }

    /* the glowing orb on the lamp, only for new who takeoffs and landings. returns how big and bright it is, 0 to 1.
       takeoff: starts pulsing 2 seconds in, and does one more pulse once the box has fully gone.
       landing: starts once the box actually begins to fade in, and stops at the end of the pulse it's on */
    private static final long ORB_TAKEOFF_DELAY_TICKS = 40L;
    private @Nullable TardisFlightState orbState;
    private long orbTransitionStart;
    private long orbStart = -1L;
    private float orbEndCycle = -1f;
    private boolean orbDone;

    public float lampOrb(float partialTick) {
        if (level == null || !isGlowing() || !(getFadeType() instanceof PulseFadeType)) return 0f;
        long now = GameClockUtil.now(level);
        TardisFlightState state = getFlightState();

        /* a new takeoff or landing starts the orb over */
        if (state.isTransitioning() && state != orbState) {
            orbState = state;
            orbTransitionStart = now;
            orbStart = -1L;
            orbEndCycle = -1f;
            orbDone = false;
        }
        if (orbDone) return 0f;

        if (orbStart < 0L) {
            if (!state.isTransitioning()) return 0f;
            boolean ready = state == TardisFlightState.TAKING_OFF
                    ? now - orbTransitionStart >= ORB_TAKEOFF_DELAY_TICKS
                    : getOpacity(partialTick) > 0.001f;
            if (!ready) return 0f;
            orbStart = now;
        }

        float cycles = (now - orbStart + partialTick) / LAMP_FLICKER_TICKS;
        /* the fade is over. takeoff does one more full pulse, landing just finishes the one it's on */
        if (orbEndCycle < 0f && fadeFinished(now)) {
            float extra = orbState == TardisFlightState.TAKING_OFF ? 2f : 0f;
            float end = (float) Math.ceil(cycles + extra);
            if (((int) end) % 2 != 0) end += 1f;
            orbEndCycle = end;
        }
        if (orbEndCycle >= 0f && cycles >= orbEndCycle) {
            orbDone = true;
            return 0f;
        }
        /* starts at size 0 and swells up into its first pulse */
        return 0.5f - 0.5f * (float) Math.cos(cycles * Math.PI);
    }

    /* the box has finished fading out (takeoff) or in (landing) */
    private boolean fadeFinished(long now) {
        return now - flight.fadeStartTick() >= flight.duration();
    }

    public boolean isGlowing() {
        return asEntity().get(TardisComponentTypes.GLOW).glowing();
    }

    public void setGlowing(boolean glowing) {
        GlowSystem.setLamp(asEntity(), glowing);
        applyGlowToBlockState(glowing);
        sync();
    }

    private void applyGlowToBlockState(boolean glowing) {
        if (level == null || level.isClientSide()) return;
        BlockState current = getBlockState();
        if (current.getValue(TardisBlock.GLOWING) != glowing) {
            level.setBlock(getBlockPos(), current.setValue(TardisBlock.GLOWING, glowing), 3);
        }
    }

    // client side, re-meshes the surrounding chunk sections whenever the shadow toggles, so the baked ambient
    // occlusion on neighbouring blocks is refreshed
    public static void clientTick(Level level, TardisExteriorBlockEntity blockEntity) {
        GCClientHooks.trackExteriorHum(blockEntity);
        boolean active = blockEntity.isShadowActive();
        if (active == blockEntity.lastShadowActive) return;
        blockEntity.lastShadowActive = active;
        BlockPos pos = blockEntity.getBlockPos();
        BlockState state = blockEntity.getBlockState();
        level.sendBlockUpdated(pos, state, state, Block.UPDATE_IMMEDIATE);
        level.sendBlockUpdated(pos.above(), state, state, Block.UPDATE_IMMEDIATE);
    }

    public static void serverTick(Level level, TardisExteriorBlockEntity blockEntity) {
        if (GameClockUtil.now(level) % 40L == 0L) {
            TardisTopBlock.placeAbove(level, blockEntity.getBlockPos());
        }
        if (level.isClientSide()) return;
        blockEntity.refreshFadeType();
        blockEntity.tickLanding(level);
        /* players whose view of this chunk changed (left the interior, came back through
           a portal) may have missed an update, so resend while it travels */
        if (blockEntity.isTravelling() && (GameClockUtil.now(level) + blockEntity.tickOffset) % TRAVEL_RESYNC_TICKS == 0) {
            blockEntity.sync();
        }
        blockEntity.updateLight(level);
        if ((GameClockUtil.now(level) + blockEntity.tickOffset) % POWER_CHECK_INTERVAL_TICKS == 0) {
            blockEntity.refreshPowerAndGlow(level);
        }
        if ((GameClockUtil.now(level) + blockEntity.tickOffset) % ENTRY_ZONE_INTERVAL_TICKS == 0) {
            TardisInteriorSystem.tickEntryZone(level, blockEntity);
        }
        if (level instanceof ServerLevel serverLevel) {
            float lockHealth = SubsystemSystem.healthOf(blockEntity.asEntity(), TardisSubsystem.DOOR_LOCK);
            if (lockHealth < SubsystemSystem.SMOKE_THRESHOLD) {
                LockDamageEffects.tick(serverLevel, blockEntity.getKeyWorldPos(), lockHealth);
            }
            blockEntity.tickTakeoffWind(serverLevel);
        }
    }

    // scales the block light with the exterior's opacity, so it fades out as the tardis dematerialises and comes
    // back as it materialises, it only touches the block state when the level actually changes
    private void updateLight(Level level) {
        BlockState current = getBlockState();
        if (!current.hasProperty(TardisBlock.LIGHT)) return;
        int target = isGlowing() ? Math.round(TardisBlock.MAX_LIGHT * getOpacity(0.0f)) : 0;
        target = Math.clamp(target, 0, TardisBlock.MAX_LIGHT);
        if (current.getValue(TardisBlock.LIGHT) != target) {
            level.setBlock(getBlockPos(), current.setValue(TardisBlock.LIGHT, target), 3);
        }
    }

    // called when the interior loses its last console, cuts the exterior's power and glow straight away
    public void cutPower() {
        if (level == null || level.isClientSide()) return;
        powered = false;
        refreshPowerAndGlow(level);
        setChanged();
    }

    private void refreshPowerAndGlow(Level level) {
        boolean wasPowered = powered;
        powered = TardisApi.of(this).flatMap(Tardis::livePower).orElse(powered);
        if (GlowSystem.tick(asEntity(), powered)) {
            applyGlowToBlockState(isGlowing());
            sync();
        } else if (powered != wasPowered) {
            /* tell the client right away, the outside hum fades with the power */
            sync();
        }
    }

    private void tickTakeoffWind(ServerLevel serverLevel) {
        if (cloak.isCloaked()) return;
        TakeoffWind.tick(serverLevel, getBlockPos(), flight.inFlight(), isFullyInvisible(), flight.fadeStartTick(), flight.duration());
    }

    private static UUID keyIdOf(ItemStack key) {
        UUID linked = key.get(GCDataComponents.TARDIS_ID);
        return linked != null ? linked : UNLINKED_KEY;
    }

    public boolean isWrongKeyLockedOut(Player player, ItemStack key) {
        if (level == null || level.isClientSide()) return false;
        Long until = wrongKeyLockouts.get(keyIdOf(key));
        if (until == null) return false;

        long remaining = until - GameClockUtil.now(level);
        if (remaining <= 0L) {
            wrongKeyLockouts.remove(keyIdOf(key));
            return false;
        }
        MessageUtil.overlay(player, ChatFormatting.RED, "key_lockout", (remaining + 19L) / 20L);
        return true;
    }

    public void insertWrongKey(Player player, InteractionHand hand, ItemStack held) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        UUID keyId = keyIdOf(held);
        long now = GameClockUtil.now(serverLevel);
        wrongKeyLockouts.values().removeIf(until -> until <= now);
        wrongKeyLockouts.put(keyId, now + WRONG_KEY_LOCKOUT_TICKS);
        TardisApi.get(serverLevel.getServer(), getTardisId()).ifPresent(TardisMoods::onWrongKey);

        insertKey(player, hand, held);

        UUID playerId = player.getUUID();
        MinecraftServer server = serverLevel.getServer();
        GCSchedulerUtil.runLater(server, WRONG_KEY_JAM_DELAY_TICKS, () -> {
            if (isRemoved() || !isWrongKeyStillInside(playerId, keyId)) return;
            triggerAnim("key_controller", "key_jammed");
            ExteriorSounds.keyJammed(level, getBlockPos());
        });
        GCSchedulerUtil.runLater(server, WRONG_KEY_RETURN_DELAY_TICKS, () -> {
            if (isRemoved() || !isWrongKeyStillInside(playerId, keyId)) return;
            ItemStack returned = clearHeldKey();
            returned.set(GCDataComponents.KEY_INSERTED, false);
            ServerPlayer owner = server.getPlayerList().getPlayer(playerId);
            if (owner == null) {
                Block.popResource(serverLevel, getBlockPos(), returned);
                return;
            }
            if (!owner.getInventory().add(returned)) {
                owner.drop(returned, false, Prediction.SERVER_ONLY);
            }
            MessageUtil.overlay(owner, ChatFormatting.RED, "wrong_key");
        });
    }

    private boolean isWrongKeyStillInside(UUID playerId, UUID keyId) {
        ItemStack inside = heldKey.stack();
        return hasKeyRendered() && playerId.equals(heldKey.owner()) && inside != null && keyIdOf(inside).equals(keyId);
    }

    public boolean rejectKeyIfLockBroken(@Nullable Player player) {
        if (SubsystemSystem.acceptsKey(asEntity(), TardisSubsystem.DOOR_LOCK)) {
            return false;
        }
        ExteriorSounds.keyJammed(level, getBlockPos());
        if (player != null) {
            MessageUtil.overlay(player, ChatFormatting.RED, "lock_too_damaged");
        }
        return true;
    }

    public void interact(boolean sneaking) {
        interact(sneaking, false);
    }

    public void interact(boolean sneaking, boolean ignoreLock) {
        if (areDoorsSealedForTravel()) {
            announceSealed();
            return;
        }
        if (!ignoreLock && isLocked()) {
            return;
        }
        DoorSystem.InteractionResult result =
                DoorSystem.interact(asEntity(), sneaking, System.currentTimeMillis());
        if (result.hasNoAnimation()) return;
        if (controller != null) {
            controller.setAnimationSpeed(1.0);
        }
        playAnimation(result.animationName());
        ExteriorSounds.door(level, getBlockPos(), result.opening());
        sync();
        if (level instanceof ServerLevel serverLevel) {
            UUID id = getTardisId();
            if (id != null) {
                DoorSyncSystem.onExteriorDoorChanged(serverLevel.getServer(), id, result.state(),
                        result.animationName(), result.opening());
            }
        }
    }

    public void applySyncedDoor(DoorState state, String animation, boolean opening) {
        DoorSystem.applyRemoteDoorState(asEntity(), state, opening, System.currentTimeMillis());
        if (controller != null) {
            controller.setAnimationSpeed(1.0);
        }
        playAnimation(animation);
        ExteriorSounds.door(level, getBlockPos(), opening);
        sync();
    }

    private void announceKeyJammed(@Nullable Player player) {
        triggerAnim("key_controller", "key_jammed");
        ExteriorSounds.keyJammed(level, getBlockPos());
        if (player != null) {
            MessageUtil.overlay(player, ChatFormatting.RED, "lock_jammed");
        }
    }

    public float getSubsystemHealth(TardisSubsystem subsystem) {
        return SubsystemSystem.healthOf(asEntity(), subsystem);
    }

    public void setSubsystemHealth(TardisSubsystem subsystem, float health) {
        SubsystemSystem.setHealth(asEntity(), subsystem, health);
    }

    private boolean lockedFeedbackThrottled() {
        long now = System.currentTimeMillis();
        if (now - lastLockedFeedbackAt < 1000L) {
            return true;
        }
        lastLockedFeedbackAt = now;
        return false;
    }

    public void announceSealed() {
        if (lockedFeedbackThrottled()) {
            return;
        }

        triggerAnim("key_controller", "is_locked");
        ExteriorSounds.locked(level, getBlockPos());
    }

    public void announceLocked(Player player) {
        if (lockedFeedbackThrottled()) {
            return;
        }

        triggerAnim("key_controller", "is_locked");
        ExteriorSounds.locked(level, getBlockPos());

        Component overlay = TardisApi.of(this)
                .map(tardis -> Component.translatable("message.gallifreyan_chronicles.locked_owned",
                        tardis.ownerName()
                                .map(name -> Component.literal(name).withStyle(ChatFormatting.AQUA))
                                .orElseGet(() -> Component.translatable("message.gallifreyan_chronicles.unknown_player").withStyle(ChatFormatting.AQUA))))
                .orElseGet(() -> Component.translatable("message.gallifreyan_chronicles.locked"));
        player.sendOverlayMessage(overlay.copy().withStyle(ChatFormatting.RED));
    }

    public void playAnimation(String animationName) {
        if (level != null && level.isClientSide()) {
            triggerAnim("anim_controller", animationName);
            return;
        }
        doorAnimSeq++;
        doorAnim = animationName;
        doorAnimInstant = false;
        sync();
    }

    @Override
    protected void saveAdditional(@NonNull ValueOutput output) {
        super.saveAdditional(output);
        components.save(output, TardisComponentTypes.ALL);
        heldKey.save(output);
        output.putBoolean("Powered", powered);
        output.putBoolean("Locked", locked);
        output.putInt("DoorAnimSeq", doorAnimSeq);
        output.putString("DoorAnim", doorAnim);
        output.putBoolean("DoorAnimInstant", doorAnimInstant);
        flight.save(output);
        cloak.save(output);
    }

    @Override
    public void loadAdditional(@NonNull ValueInput input) {
        super.loadAdditional(input);
        components.load(input, TardisComponentTypes.ALL);
        heldKey.load(input);
        powered = input.getBooleanOr("Powered", true);
        locked = input.getBooleanOr("Locked", false);
        flight.load(input);
        cloak.load(input);
        portalActive = input.getBooleanOr("PortalActive", false);
        int animSeq = input.getIntOr("DoorAnimSeq", 0);
        String anim = input.getStringOr("DoorAnim", "");
        boolean instant = input.getBooleanOr("DoorAnimInstant", false);
        if (doorAnimSeqKnown && animSeq != doorAnimSeq && !anim.isEmpty()
                && level != null && level.isClientSide()) {
            if (instant) {
                startSnapClient();
            } else if (controller != null) {
                controller.setAnimationSpeed(1.0);
            }
            triggerAnim("anim_controller", anim);
        }
        doorAnimSeq = animSeq;
        doorAnim = anim;
        doorAnimInstant = instant;
        doorAnimSeqKnown = true;
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        discardKeyEntity();
    }

    public void sync() {
        BlockEntitySyncUtil.sendToClients(this);
    }

    @Override
    public @NonNull CompoundTag getUpdateTag(HolderLookup.@NonNull Provider registries) {
        CompoundTag tag = saveWithoutMetadata(registries);
        tag.putBoolean("PortalActive", portalActive);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        controller = new AnimationController<>(
                "anim_controller",
                0,
                state -> {
                    if (needsStateSync && level != null && level.isClientSide()) {
                        DoorState doorState = getDoorState();
                        String animName = getAnimationForDoorState(doorState);
                        snapAnimation(state.controller(), animName);
                        needsStateSync = false;
                    } else if (snapFramesLeft > 0 && --snapFramesLeft == 0) {
                        state.controller().setAnimationSpeed(1.0);
                    }
                    return PlayState.CONTINUE;
                }
        );
        controller.triggerableAnim("open_left", RawAnimation.begin().thenPlay("open_left"))
                .triggerableAnim("open_right", RawAnimation.begin().thenPlay("open_right"))
                .triggerableAnim("open_all", RawAnimation.begin().thenPlay("open_all"))
                .triggerableAnim("close_left", RawAnimation.begin().thenPlay("close_left"))
                .triggerableAnim("close_right", RawAnimation.begin().thenPlay("close_right"))
                .triggerableAnim("close_left_shift", RawAnimation.begin().thenPlay("close_left_shift"))
                .triggerableAnim("close_right_shift", RawAnimation.begin().thenPlay("close_right_shift"))
                .triggerableAnim("close_all", RawAnimation.begin().thenPlay("close_all"));
        registrar.add(controller);

        AnimationController<TardisExteriorBlockEntity> keyController = new AnimationController<>("key_controller",
                0,
                _ -> PlayState.CONTINUE);
        keyController.triggerableAnim("key_lock", RawAnimation.begin().thenPlay("key_lock"))
                .triggerableAnim("key_unlock", RawAnimation.begin().thenPlay("key_unlock"))
                .triggerableAnim("is_locked", RawAnimation.begin().thenPlay("is_locked"))
                .triggerableAnim("key_jammed", RawAnimation.begin().thenPlay("key_jammed"));
        registrar.add(keyController);

        if (level != null && level.isClientSide()) {
            snapToState();
        }
    }

    @Override
    public @NotNull AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
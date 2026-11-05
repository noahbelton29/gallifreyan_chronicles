package com.noahtnt2009.gallifreyan_chronicles.block.entity;

import com.noahtnt2009.gallifreyan_chronicles.block.TardisTopBlock;
import com.noahtnt2009.gallifreyan_chronicles.tardis.door.TardisDoorRegistry;
import com.noahtnt2009.gallifreyan_chronicles.tardis.api.Tardis;
import com.noahtnt2009.gallifreyan_chronicles.tardis.api.TardisApi;
import com.geckolib.animatable.GeoBlockEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.util.GeckoLibUtil;
import com.noahtnt2009.gallifreyan_chronicles.block.TardisBlock;
import com.noahtnt2009.gallifreyan_chronicles.client.renderer.geo_layer.TardisExteriorKeyLayer;
import com.noahtnt2009.gallifreyan_chronicles.ecs.ComponentHolder;
import com.noahtnt2009.gallifreyan_chronicles.ecs.ComponentType;
import com.noahtnt2009.gallifreyan_chronicles.ecs.ComponentStore;
import com.noahtnt2009.gallifreyan_chronicles.ecs.Entity;
import com.noahtnt2009.gallifreyan_chronicles.entity.TardisKeyEntity;
import com.noahtnt2009.gallifreyan_chronicles.init.GCDataComponents;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.TardisLinkable;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.component.DoorState;
import com.noahtnt2009.gallifreyan_chronicles.tardis.flight.TardisFlightState;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.TardisComponentTypes;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.component.TardisSubsystem;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.component.TransformComponent;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.system.DoorSyncSystem;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.system.DoorSystem;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.system.ExteriorSystem;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.system.GlowSystem;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.system.SubsystemSystem;
import com.noahtnt2009.gallifreyan_chronicles.init.GCGameRules;
import com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.TardisExterior;
import com.noahtnt2009.gallifreyan_chronicles.tardis.interior.TardisInteriorSystem;
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
import com.noahtnt2009.gallifreyan_chronicles.client.renderer.CloakKeyReveal;
import com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.ExteriorFlight;
import com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.ExteriorSounds;
import com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.HeldKey;
import com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.LockDamageEffects;
import com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.TakeoffWind;
import com.noahtnt2009.gallifreyan_chronicles.util.FadingValue;
import com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.fade.CloakFade;
import com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.fade.FadeType;
import org.jspecify.annotations.NonNull;
import java.util.UUID;
import java.util.function.Supplier;

public class TardisExteriorBlockEntity extends BlockEntity implements GeoBlockEntity, ComponentHolder, TardisLinkable,
        TardisExteriorKeyLayer.TardisExteriorRenderable {
    public static Supplier<BlockEntityType<TardisExteriorBlockEntity>> TYPE;
    private static final long KEY_JAM_COOLDOWN_TICKS = 40L;
    private static final long KEY_TURN_COOLDOWN_TICKS = 20L;
    private long lastLockedFeedbackAt = 0L;
    private long keyCooldownUntil = 0L;
    private AnimationController<TardisExteriorBlockEntity> controller;
    private boolean locked = true;
    private static final int POWER_CHECK_INTERVAL_TICKS = 10;
    private static final int ENTRY_ZONE_INTERVAL_TICKS = 2;
    private boolean powered = true;
    private boolean portalActive = false;
    private int doorAnimSeq = 0;
    private String doorAnim = "";
    private boolean doorAnimSeqKnown = false;
    private final int tickOffset;
    private final ExteriorFlight flight = new ExteriorFlight();
    private boolean lastShadowActive = true;
    private final HeldKey heldKey = new HeldKey();
    private final CloakFade cloak = new CloakFade();
    private static final float KEY_REVEAL_OPACITY = 0.2f;
    private final FadingValue keyReveal = new FadingValue(0.5f, 0.0f);
    private final ComponentStore components = new ComponentStore();
    private final Entity ecsEntity = Entity.of(this, this::sync);
    private AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private boolean needsStateSync = false;
    private TardisKeyEntity keyEntity = null;
    private volatile Vec3 cachedKeyBoneWorldPos = null;

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

    public boolean areDoorsFullyClosed() {
        return getDoorState().isClosed()
                && !DoorSystem.isAnimating(asEntity(), System.currentTimeMillis());
    }

    public TardisFlightState getFlightState() {
        if (level != null && !level.isClientSide() && flight.expireState(level.getGameTime())) {
            setChanged();
        }
        return flight.state();
    }

    public void setFlightState(TardisFlightState state, long durationTicks) {
        if (level == null || level.isClientSide()) return;
        flight.setState(state, level.getGameTime(), durationTicks);
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
            long now = level.getGameTime();
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
        String doorVariant = TardisDoorRegistry.interiorVariantForExterior(exterior.id());
        if (doorVariant == null) return;
        for (InteriorDoorBlockEntity door : DoorSyncSystem.doorsOf(id)) {
            if (door.isLinked()) door.setDoorVariant(doorVariant);
        }
    }

    // used by the summon command to move a tardis to a new block
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void copyStateFrom(TardisExteriorBlockEntity other) {
        for (ComponentType<?> type : TardisComponentTypes.ALL) {
            com.noahtnt2009.gallifreyan_chronicles.ecs.Component value = other.components.getOrNull((ComponentType) type);
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
        target.setAnimationSpeed(999999);
        new Thread(() -> {
            try {
                Thread.sleep(50);
                if (controller != null) {
                    controller.setAnimationSpeed(1.0);
                }
            } catch (InterruptedException ignored) {}
        }).start();
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
        String animName = getAnimationForDoorState(state);
        playAnimation(animName);
    }

    public float getYaw() {
        return asEntity().get(TardisComponentTypes.TRANSFORM).yaw();
    }

    public void setYaw(float yaw) {
        asEntity().set(TardisComponentTypes.TRANSFORM, new TransformComponent(yaw));
    }

    public void setInFlight(boolean inFlight) {
        long now = level != null ? level.getGameTime() : 0L;

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
        if (flight.setInFlight(inFlight, atTick)) sync();
    }

    public void scheduleLanding(int delayTicks) {
        if (level == null || level.isClientSide()) return;
        if (flight.scheduleLanding(level.getGameTime(), delayTicks)) setChanged();
    }

    public void cancelLanding() {
        if (flight.cancelLanding()) setChanged();
    }

    private void tickLanding(Level level) {
        long dueTick = flight.consumeDueLanding(level.getGameTime());
        if (dueTick < 0L) return;
        setInFlight(false, dueTick);
        setChanged();
    }

    public FadeType getFadeType() {
        return flight.fadeType();
    }

    public void setFadeType(FadeType fadeType) {
        boolean serverSide = level != null && !level.isClientSide();
        long now = serverSide ? level.getGameTime() : 0L;
        if (flight.setFadeType(fadeType, now, serverSide)) sync();
    }

    private void refreshFadeType() {
        if (level != null && flight.refreshFadeType(level.getGameTime())) sync();
    }

    public boolean isCloaked() {
        return cloak.isCloaked();
    }

    public void setCloaked(boolean cloaked) {
        if (level == null || level.isClientSide()) return;
        if (cloak.set(cloaked, level.getGameTime())) sync();
    }

    private float cloakOpacity(float partialTick) {
        if (level == null) return cloak.restingOpacity();
        return cloak.opacity(level.getGameTime(), partialTick);
    }

    public boolean isShadowActive() {
        if (level == null) return true;
        return !flight.inFlight() && flight.opacity(level.getGameTime(), 0.0f) >= 1.0f;
    }

    public boolean isFullyInvisible() {
        if (level == null) return false;
        return flight.isFullyInvisible(level.getGameTime());
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
        return flight.opacity(level.getGameTime(), partialTick);
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

    public boolean isGlowing() {
        return asEntity().get(TardisComponentTypes.GLOW).glowing();
    }

    public void setGlowing(boolean glowing) {
        GlowSystem.setManualGlow(asEntity(), glowing);
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
        boolean active = blockEntity.isShadowActive();
        if (active == blockEntity.lastShadowActive) return;
        blockEntity.lastShadowActive = active;
        BlockPos pos = blockEntity.getBlockPos();
        BlockState state = blockEntity.getBlockState();
        level.sendBlockUpdated(pos, state, state, Block.UPDATE_IMMEDIATE);
        level.sendBlockUpdated(pos.above(), state, state, Block.UPDATE_IMMEDIATE);
    }

    public static void serverTick(Level level, TardisExteriorBlockEntity blockEntity) {
        if (level.getGameTime() % 40L == 0L) {
            TardisTopBlock.placeAbove(level, blockEntity.getBlockPos());
        }
        if (level.isClientSide()) return;
        blockEntity.refreshFadeType();
        blockEntity.tickLanding(level);
        blockEntity.updateLight(level);
        if ((level.getGameTime() + blockEntity.tickOffset) % POWER_CHECK_INTERVAL_TICKS == 0) {
            blockEntity.refreshPowerAndGlow(level);
        }
        if ((level.getGameTime() + blockEntity.tickOffset) % ENTRY_ZONE_INTERVAL_TICKS == 0) {
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
        powered = TardisApi.of(this).flatMap(Tardis::livePower).orElse(powered);
        MinecraftServer server = level.getServer();
        boolean daylightAffectsGlow = server == null
                || server.getGameRules().get(GCGameRules.DAYLIGHT_CYCLE_AFFECTS_GLOW);
        if (GlowSystem.tick(asEntity(), level.getDefaultClockTime(), daylightAffectsGlow, powered)) {
            applyGlowToBlockState(isGlowing());
            sync();
        }
    }

    private void tickTakeoffWind(ServerLevel serverLevel) {
        if (cloak.isCloaked()) return;
        TakeoffWind.tick(serverLevel, getBlockPos(), flight.inFlight(), isFullyInvisible(), flight.fadeStartTick(), flight.duration());
    }

    public boolean rejectKeyIfLockBroken(@Nullable Player player) {
        if (SubsystemSystem.acceptsKey(asEntity(), TardisSubsystem.DOOR_LOCK)) {
            return false;
        }
        ExteriorSounds.keyJammed(level, getBlockPos());
        if (player != null) {
            player.sendOverlayMessage(
                    Component.translatable("message.gallifreyan_chronicles.lock_too_damaged").withStyle(ChatFormatting.RED));
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
            player.sendOverlayMessage(Component.translatable("message.gallifreyan_chronicles.lock_jammed").withStyle(ChatFormatting.RED));
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
        flight.save(output);
        cloak.save(output);
    }

    @Override
    public void loadAdditional(@NonNull ValueInput input) {
        super.loadAdditional(input);
        components.load(input, TardisComponentTypes.ALL);
        heldKey.load(input);
        powered = input.getBooleanOr("Powered", true);
        locked = input.getBooleanOr("Locked", true);
        flight.load(input);
        cloak.load(input);
        portalActive = input.getBooleanOr("PortalActive", false);
        int animSeq = input.getIntOr("DoorAnimSeq", 0);
        String anim = input.getStringOr("DoorAnim", "");
        if (doorAnimSeqKnown && animSeq != doorAnimSeq && !anim.isEmpty()
                && level != null && level.isClientSide()) {
            if (controller != null) {
                controller.setAnimationSpeed(1.0);
            }
            triggerAnim("anim_controller", anim);
        }
        doorAnimSeq = animSeq;
        doorAnim = anim;
        doorAnimSeqKnown = true;
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        discardKeyEntity();
    }

    public void sync() {
        if (level != null) {
            setChanged();
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
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
package com.noahtnt2009.gallifreyan_chronicles.block.entity;

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
import com.noahtnt2009.gallifreyan_chronicles.init.GCSounds;
import com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.TardisExterior;
import com.noahtnt2009.gallifreyan_chronicles.tardis.interior.TardisInteriorSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.fade.FadeType;
import com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.fade.FadeTypeKinds;
import com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.fade.FadeTypes;
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
    private boolean inFlight = false;
    private TardisFlightState flightState = TardisFlightState.IDLE;
    private long flightStateEndTick = 0L;
    private long fadeStartTick = 0L;
    private long landAtTick = -1L;
    private FadeType fadeType = FadeTypes.DEFAULT;
    private int seenFadeGeneration = -1;
    private final ComponentStore components = new ComponentStore();
    private AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private boolean needsStateSync = false;
    private UUID renderedKeyOwner = null;
    private ItemStack heldKeyStack = ItemStack.EMPTY;
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
    }

    public boolean hasKeyRendered() {
        return renderedKeyOwner != null;
    }

    public boolean areDoorsFullyClosed() {
        return getDoorState().isClosed()
                && !DoorSystem.isAnimating(asEntity(), System.currentTimeMillis());
    }

    public TardisFlightState getFlightState() {
        if (flightState.isTimed() && level != null && !level.isClientSide()
                && level.getGameTime() >= flightStateEndTick) {
            flightState = flightState.afterTimer();
            setChanged();
        }
        return flightState;
    }

    public void setFlightState(TardisFlightState state, long durationTicks) {
        if (level == null || level.isClientSide()) return;
        flightState = state;
        flightStateEndTick = level.getGameTime() + durationTicks;
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
        playKeySound();
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
        playKeySound();
        sync();

        if (level instanceof ServerLevel serverLevel) {
            UUID id = getTardisId();
            if (id != null) {
                DoorSyncSystem.onLockChanged(serverLevel.getServer(), id, nowLocked, true);
            }
        }
    }

    public @Nullable UUID getRenderedKeyOwner() {
        return renderedKeyOwner;
    }

    @Override
    public @Nullable ItemStack gc$getHeldKeyStack() {
        return heldKeyStack;
    }

    public void setHeldKey(UUID owner, ItemStack stack) {
        this.renderedKeyOwner = owner;
        this.heldKeyStack = stack.copy();
        sync();
    }

    public ItemStack clearHeldKey() {
        ItemStack returned = this.heldKeyStack.copy();
        this.renderedKeyOwner = null;
        this.heldKeyStack = ItemStack.EMPTY;
        sync();
        return returned;
    }

    @Override
    public ComponentStore componentStore() {
        return components;
    }

    public Entity asEntity() {
        return Entity.of(this, this::sync);
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
        this.inFlight = other.inFlight;
        this.fadeStartTick = other.fadeStartTick;
        this.landAtTick = other.landAtTick;
        this.flightState = other.flightState;
        this.flightStateEndTick = other.flightStateEndTick;
        this.fadeType = other.fadeType;
        this.renderedKeyOwner = other.renderedKeyOwner;
        this.heldKeyStack = other.heldKeyStack.copy();
        resetAnimationState();
    }

    private void snapToState() {
        if (level == null || !level.isClientSide() || controller == null) return;
        DoorState state = getDoorState();
        String animName = getAnimationForDoorState(state);
        controller.setAnimation(RawAnimation.begin().thenPlay(animName));
        controller.setAnimationSpeed(999999);
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
    
    private long fadeDuration() {
        return inFlight ? fadeType.timing().fadeOutTicks() : fadeType.timing().fadeInTicks();
    }

    public void setInFlight(boolean inFlight) {
        if (level == null || level.isClientSide()) return;
        setInFlight(inFlight, level.getGameTime());
    }

    private void setInFlight(boolean inFlight, long atTick) {
        if (this.inFlight == inFlight || level == null || level.isClientSide()) return;

        long now = atTick;
        float oldProgress = Math.clamp((now - fadeStartTick) / (float) fadeDuration(), 0.0f, 1.0f);
        float gone = this.inFlight ? oldProgress : 1.0f - oldProgress;

        this.inFlight = inFlight;
        float newProgress = inFlight ? gone : 1.0f - gone;
        this.fadeStartTick = now - Math.round(newProgress * fadeDuration());
        if (inFlight && gone <= 0.0f) {
            this.fadeStartTick = now + fadeType.timing().takeoffDelayTicks();
        }
        sync();
    }

    public void scheduleLanding(int delayTicks) {
        if (level == null || level.isClientSide() || !inFlight) return;
        landAtTick = level.getGameTime() + delayTicks;
        setChanged();
    }

    public void cancelLanding() {
        if (landAtTick < 0L) return;
        landAtTick = -1L;
        setChanged();
    }

    private void tickLanding(Level level) {
        if (landAtTick < 0L || level.getGameTime() < landAtTick) return;
        long dueTick = landAtTick;
        landAtTick = -1L;
        setInFlight(false, dueTick);
        setChanged();
    }

    public FadeType getFadeType() {
        return fadeType;
    }

    public void setFadeType(FadeType fadeType) {
        if (this.fadeType == fadeType) return;

        if (level != null && !level.isClientSide()) {
            long now = level.getGameTime();
            if (fadeStartTick <= now) {
                float progress = Math.clamp((now - fadeStartTick) / (float) fadeDuration(), 0.0f, 1.0f);
                this.fadeType = fadeType;
                this.fadeStartTick = now - Math.round(progress * fadeDuration());
                sync();
                return;
            }
        }
        this.fadeType = fadeType;
        sync();
    }

    private void refreshFadeType() {
        int generation = FadeTypes.generation();
        if (seenFadeGeneration == generation) return;
        seenFadeGeneration = generation;

        String id = fadeType.id();
        if (!FadeTypes.contains(id)) return;
        setFadeType(FadeTypes.get(id));
    }

    public boolean isFullyInvisible() {
        return inFlight && level != null && level.getGameTime() - fadeStartTick >= fadeType.timing().fadeOutTicks();
    }

    public float getOpacity(float partialTick) {
        if (level == null) return inFlight ? 0.0f : 1.0f;

        float elapsed = (level.getGameTime() - fadeStartTick) + partialTick;
        float progress = Math.clamp(elapsed / fadeDuration(), 0.0f, 1.0f);
        if (progress >= 1.0f) return inFlight ? 0.0f : 1.0f;

        float gone = inFlight ? progress : 1.0f - progress;
        return fadeType.opacity(gone, elapsed);
    }

    public @Nullable UUID getTardisId() {
        return getLinkedTardisId().orElse(null);
    }

    public DoorState getDoorState() {
        return DoorSystem.stateOf(asEntity());
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

    public static void serverTick(Level level, TardisExteriorBlockEntity blockEntity) {
        if (level.isClientSide()) return;
        blockEntity.refreshFadeType();
        blockEntity.tickLanding(level);
        MinecraftServer server = level.getServer();
        boolean daylightAffectsGlow = server == null
                || server.getGameRules().get(GCGameRules.DAYLIGHT_CYCLE_AFFECTS_GLOW);
        if (GlowSystem.tick(blockEntity.asEntity(), level.getDefaultClockTime(), daylightAffectsGlow)) {
            blockEntity.applyGlowToBlockState(blockEntity.isGlowing());
            blockEntity.sync();
        }
        TardisInteriorSystem.tickEntryZone(level, blockEntity);
        if (level instanceof ServerLevel serverLevel) {
            blockEntity.tickLockDamageEffects(serverLevel);
            blockEntity.tickTakeoffWind(serverLevel);
        }
    }

    private static final int WIND_MAX_DUST = 10;
    private static final double WIND_DUST_RADIUS = 0.95;
    private static final float WIND_DUST_SPEED = 0.2f;
    private static final double WIND_CLOUD_RADIUS = 0.6;
    private static final float WIND_CLOUD_SPEED = 0.08f;
    private static final float WIND_END_PROGRESS = 0.92f;

    private void tickTakeoffWind(ServerLevel serverLevel) {
        long now = serverLevel.getGameTime();
        float progress = Math.clamp((now - fadeStartTick) / (float) fadeDuration(), 0.0f, 1.0f);

        float strength;
        if (inFlight) {
            if (isFullyInvisible() || progress >= WIND_END_PROGRESS) return;
            strength = 1.0f - progress / WIND_END_PROGRESS;
            strength *= strength;
        } else {
            if (progress >= 1.0f || fadeStartTick > now) return;
            strength = progress * progress;
        }

        if (now % 3L != 0L) return;

        double cx = getBlockPos().getX() + 0.5;
        double cy = getBlockPos().getY() + 0.05;
        double cz = getBlockPos().getZ() + 0.5;

        BlockState ground = serverLevel.getBlockState(getBlockPos().below());
        ParticleOptions dust = ground.isAir() || ground.getRenderShape() == RenderShape.INVISIBLE
                ? ParticleTypes.CLOUD
                : new BlockParticleOption(ParticleTypes.BLOCK, ground);

        var random = serverLevel.getRandom();
        double spin = now * 0.15;

        int dustPoints = Math.max(inFlight ? 2 : 1, Math.round(WIND_MAX_DUST * strength));
        float dustSpeed = WIND_DUST_SPEED * (0.5f + 0.5f * strength);
        for (int i = 0; i < dustPoints; i++) {
            double angle = spin + (Math.PI * 2.0 * i) / dustPoints + (random.nextDouble() - 0.5) * 0.3;
            double dx = Math.cos(angle);
            double dz = Math.sin(angle);
            serverLevel.sendParticles(dust,
                    cx + dx * WIND_DUST_RADIUS, cy, cz + dz * WIND_DUST_RADIUS,
                    0, dx, 0.0, dz, dustSpeed);
        }

        int clouds = strength > 0.5f ? 2 : 1;
        for (int i = 0; i < clouds; i++) {
            double angle = random.nextDouble() * Math.PI * 2.0;
            double dx = Math.cos(angle);
            double dz = Math.sin(angle);
            serverLevel.sendParticles(ParticleTypes.CLOUD,
                    cx + dx * WIND_CLOUD_RADIUS, cy, cz + dz * WIND_CLOUD_RADIUS,
                    0, dx, 0.0, dz, WIND_CLOUD_SPEED * (0.6f + 0.4f * strength));
        }
    }

    private void tickLockDamageEffects(ServerLevel serverLevel) {
        float health = SubsystemSystem.healthOf(asEntity(), TardisSubsystem.DOOR_LOCK);
        if (health >= SubsystemSystem.SMOKE_THRESHOLD) {
            return;
        }

        var random = serverLevel.getRandom();
        Vec3 pos = getKeyWorldPos();

        float smoke = (SubsystemSystem.SMOKE_THRESHOLD - health) / SubsystemSystem.SMOKE_THRESHOLD;
        if (random.nextFloat() < 0.04f + 0.40f * smoke) {
            serverLevel.sendParticles(ParticleTypes.SMOKE,
                    pos.x, pos.y, pos.z, 1, 0.06, 0.06, 0.06, 0.01);
        }

        if (health <= 0f) {
            serverLevel.sendParticles(ParticleTypes.FLAME,
                    pos.x, pos.y, pos.z, 3, 0.12, 0.12, 0.12, 0.02);
            if (random.nextFloat() < 0.35f) {
                serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE,
                        pos.x, pos.y + 0.1, pos.z, 1, 0.08, 0.08, 0.08, 0.03);
            }
        } else if (health < SubsystemSystem.FIRE_THRESHOLD) {
            float fire = (SubsystemSystem.FIRE_THRESHOLD - health) / SubsystemSystem.FIRE_THRESHOLD;
            if (random.nextFloat() < 0.03f + 0.15f * fire) {
                serverLevel.sendParticles(ParticleTypes.FLAME,
                        pos.x, pos.y, pos.z, 1, 0.05, 0.05, 0.05, 0.01);
            }
        }
    }

    public boolean rejectKeyIfLockBroken(@Nullable Player player) {
        if (SubsystemSystem.acceptsKey(asEntity(), TardisSubsystem.DOOR_LOCK)) {
            return false;
        }
        if (level != null) {
            level.playSound(null, getBlockPos(), GCSounds.TARDIS_KEY_JAMMED, SoundSource.BLOCKS, 1.0f, 1.0f);
        }
        if (player != null) {
            player.sendOverlayMessage(
                    Component.literal("The lock is too damaged to take the key!").withStyle(ChatFormatting.RED));
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
        playDoorSound(result.opening());
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
        playDoorSound(opening);
        sync();
    }

    private void announceKeyJammed(@Nullable Player player) {
        triggerAnim("key_controller", "key_jammed");
        if (level != null) {
            level.playSound(null, getBlockPos(), GCSounds.TARDIS_KEY_JAMMED, SoundSource.BLOCKS, 1.0f, 1.0f);
        }
        if (player != null) {
            player.sendOverlayMessage(Component.literal("The lock is jammed!").withStyle(ChatFormatting.RED));
        }
    }

    public float getSubsystemHealth(TardisSubsystem subsystem) {
        return SubsystemSystem.healthOf(asEntity(), subsystem);
    }

    public void setSubsystemHealth(TardisSubsystem subsystem, float health) {
        SubsystemSystem.setHealth(asEntity(), subsystem, health);
    }

    public void announceSealed() {
        long now = System.currentTimeMillis();
        if (now - lastLockedFeedbackAt < 1000L) {
            return;
        }
        lastLockedFeedbackAt = now;

        triggerAnim("key_controller", "is_locked");
        playLockedSound();
    }

    public void announceLocked(Player player) {
        long now = System.currentTimeMillis();
        if (now - lastLockedFeedbackAt < 1000L) {
            return;
        }
        lastLockedFeedbackAt = now;

        triggerAnim("key_controller", "is_locked");
        playLockedSound();

        Component overlay = Component.literal("Locked, ").withStyle(ChatFormatting.RED)
                .append(ownerDisplayName());
        player.sendOverlayMessage(overlay);
    }

    private Component ownerDisplayName() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return Component.literal("this TARDIS");
        }

        var tardis = resolveTardis(serverLevel);
        if (tardis.isEmpty()) {
            return Component.literal("this TARDIS");
        }

        UUID ownerId = tardis.get().ownerId();
        var owner = serverLevel.getServer().getPlayerList().getPlayer(ownerId);
        String ownerName = owner != null ? owner.getGameProfile().name() : "an unknown Player";

        return Component.literal("this TARDIS belongs to ")
                .append(Component.literal(ownerName).withStyle(ChatFormatting.AQUA));
    }

    private void playLockedSound() {
        if (level == null) return;
        level.playSound(null, getBlockPos(), GCSounds.TARDIS_IS_LOCKED, SoundSource.BLOCKS, 1.0f, 1.0f);
    }

    private void playKeySound() {
        if (level == null) return;
        SoundEvent sound = locked
                ? GCSounds.TARDIS_DOOR_LOCK
                : GCSounds.TARDIS_DOOR_UNLOCK;
        level.playSound(
                null,
                getBlockPos(),
                sound,
                SoundSource.BLOCKS,
                1.0f,
                1.0f
        );
    }

    private void playDoorSound(boolean opening) {
        if (level == null) return;
        SoundEvent sound = opening
                ? GCSounds.TARDIS_DOOR_OPEN
                : GCSounds.TARDIS_DOOR_CLOSE;
        level.playSound(
                null,
                getBlockPos(),
                sound,
                SoundSource.BLOCKS,
                1.0f,
                1.0f
        );
    }

    public void playAnimation(String animationName) {
        triggerAnim("anim_controller", animationName);
    }

    @Override
    protected void saveAdditional(@NonNull ValueOutput output) {
        super.saveAdditional(output);
        components.save(output, TardisComponentTypes.ALL);
        if (renderedKeyOwner != null) {
            output.putString("RenderedKeyOwner", renderedKeyOwner.toString());
            output.store("HeldKeyStack", ItemStack.OPTIONAL_CODEC, heldKeyStack);
        }
        output.putBoolean("Locked", locked);
        output.putBoolean("InFlight", inFlight);
        output.putLong("FadeStartTick", fadeStartTick);
        output.putLong("LandAtTick", landAtTick);
        output.putString("FlightState", flightState.name());
        output.putLong("FlightStateEnd", flightStateEndTick);
        output.putLong("LandAtTick", landAtTick);
        output.putString("FadeType", fadeType.id());
        output.store("Fade", FadeTypeKinds.CODEC, fadeType);
    }

    @Override
    public void loadAdditional(@NonNull ValueInput input) {
        super.loadAdditional(input);
        components.load(input, TardisComponentTypes.ALL);
        input.getString("RenderedKeyOwner").ifPresent(s -> renderedKeyOwner = UUID.fromString(s));
        heldKeyStack = input.read("HeldKeyStack", ItemStack.OPTIONAL_CODEC)
                .orElse(ItemStack.EMPTY);
        locked = input.getBooleanOr("Locked", true);
        inFlight = input.getBooleanOr("InFlight", false);
        fadeStartTick = input.getLongOr("FadeStartTick", 0L);
        landAtTick = input.getLongOr("LandAtTick", -1L);
        flightState = TardisFlightState.parse(input.getStringOr("FlightState", ""),
                inFlight ? TardisFlightState.IN_VORTEX : TardisFlightState.IDLE);
        flightStateEndTick = input.getLongOr("FlightStateEnd", 0L);
        landAtTick = input.getLongOr("LandAtTick", -1L);
        String fadeId = input.getStringOr("FadeType", FadeTypes.DEFAULT.id());
        fadeType = input.read("Fade", FadeTypeKinds.CODEC)
                .map(fade -> fade.withId(fadeId))
                .orElseGet(() -> FadeTypes.get(fadeId));
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
        return saveWithoutMetadata(registries);
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
                        state.controller().setAnimation(
                                RawAnimation.begin().thenPlay(animName)
                        );
                        state.controller().setAnimationSpeed(999999);
                        new Thread(() -> {
                            try {
                                Thread.sleep(50);
                                if (controller != null) {
                                    controller.setAnimationSpeed(1.0);
                                }
                            } catch (InterruptedException ignored) {}
                        }).start();
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
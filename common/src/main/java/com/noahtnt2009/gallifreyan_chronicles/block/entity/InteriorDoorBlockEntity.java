package com.noahtnt2009.gallifreyan_chronicles.block.entity;

import java.util.Optional;
import com.noahtnt2009.gallifreyan_chronicles.tardis.api.TardisApi;
import com.noahtnt2009.gallifreyan_chronicles.tardis.api.Tardis;
import com.geckolib.animatable.GeoBlockEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.util.GeckoLibUtil;
import com.noahtnt2009.gallifreyan_chronicles.block.InteriorDoorBlock;
import com.noahtnt2009.gallifreyan_chronicles.compat.portal.TardisPortalBridge;
import com.noahtnt2009.gallifreyan_chronicles.ecs.ComponentHolder;
import com.noahtnt2009.gallifreyan_chronicles.ecs.ComponentStore;
import com.noahtnt2009.gallifreyan_chronicles.ecs.ComponentType;
import com.noahtnt2009.gallifreyan_chronicles.ecs.Entity;
import com.noahtnt2009.gallifreyan_chronicles.init.GCSounds;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.TardisComponentTypes;
import com.noahtnt2009.gallifreyan_chronicles.tardis.door.TardisDoorRegistry;
import com.noahtnt2009.gallifreyan_chronicles.tardis.door.TardisDoorVariant;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.component.DoorState;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.component.DoorVariantComponent;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.system.DoorSyncSystem;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.system.DoorSystem;
import com.noahtnt2009.gallifreyan_chronicles.tardis.interior.TardisInterior;
import com.noahtnt2009.gallifreyan_chronicles.tardis.interior.TardisInteriorSystem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class InteriorDoorBlockEntity extends BlockEntity implements GeoBlockEntity, ComponentHolder {
    public static Supplier<BlockEntityType<InteriorDoorBlockEntity>> TYPE;
    private static final List<ComponentType<?>> SAVED = List.of(TardisComponentTypes.DOOR, TardisComponentTypes.DOOR_VARIANT);

    private final ComponentStore components = new ComponentStore();
    private AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private AnimationController<InteriorDoorBlockEntity> controller;
    private static final String LOCK_CONTROLLER = "lock_controller";
    private boolean needsStateSync = false;
    private boolean pulledFromExterior = false;
    private @Nullable UUID tardisId = null;
    private long lastLockedFeedbackAt = 0L;
    private int doorAnimSeq = 0;
    private String doorAnim = "";
    private boolean doorAnimSeqKnown = false;

    private static final int EXIT_CHECK_INTERVAL_TICKS = 4;
    private static final double EXIT_NEAR = -0.5;
    private static final double EXIT_FAR = 0.5;
    private static final double EXIT_HALF_WIDTH = 1.0;
    private static final double EXIT_BOTTOM = -0.5;

    public InteriorDoorBlockEntity(BlockPos pos, BlockState state) {
        super(TYPE.get(), pos, state);
    }

    @Override
    public ComponentStore componentStore() {
        return components;
    }

    public Entity asEntity() {
        return Entity.of(this, this::sync);
    }

    public String getVariant() {
        InteriorDoorBlock block = (InteriorDoorBlock) getBlockState().getBlock();
        if (block.isLinked()) {
            TardisDoorVariant variant = TardisDoorRegistry.resolveInterior(storedVariantId());
            if (variant != null) return TardisDoorRegistry.localId(variant.parent());
        }
        return block.variant();
    }

    private String storedVariantId() {
        return asEntity().get(TardisComponentTypes.DOOR_VARIANT).variantId();
    }

    public String getDoorVariantId() {
        String fixed = ((InteriorDoorBlock) getBlockState().getBlock()).fixedVariantId();
        if (!fixed.isEmpty()) return fixed;
        if (isLinked()) {
            TardisDoorVariant variant = TardisDoorRegistry.resolveInterior(storedVariantId());
            return variant != null ? variant.localId() : storedVariantId();
        }
        return storedVariantId();
    }

    public void setDoorVariant(String variantId) {
        if (isLinked()) {
            String resolved = TardisDoorRegistry.isInteriorVariant(variantId)
                    ? TardisDoorRegistry.localId(variantId)
                    : TardisDoorRegistry.defaultInteriorVariantId();
            asEntity().set(TardisComponentTypes.DOOR_VARIANT, new DoorVariantComponent(resolved));
            return;
        }
        String model = getVariant();
        String resolved = TardisDoorRegistry.hasVariant(model, variantId)
                ? variantId
                : TardisDoorRegistry.defaultVariantId(model);
        asEntity().set(TardisComponentTypes.DOOR_VARIANT, new DoorVariantComponent(resolved));
    }

    public String getTextureId() {
        return TardisDoorRegistry.textureIdFor(getVariant(), getDoorVariantId());
    }

    public boolean isLinked() {
        return ((InteriorDoorBlock) getBlockState().getBlock()).isLinked();
    }

    public boolean isFlush() {
        return InteriorDoorBlock.isFlush(getBlockState());
    }

    public boolean isFlipped() {
        return InteriorDoorBlock.isFlipped(getBlockState());
    }

    public Direction getFacing() {
        return getBlockState().getValue(InteriorDoorBlock.FACING);
    }

    public DoorState getDoorState() {
        return DoorSystem.stateOf(asEntity());
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

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (tardisId != null) {
            DoorSyncSystem.unregister(tardisId, this);
        }
    }

    public void serverTick() {
        if (!(level instanceof ServerLevel serverLevel)) return;
        if (!pulledFromExterior) {
            pulledFromExterior = true;
            if (!isLinked()) return;
            InteriorDoorBlock.placeBarriers(serverLevel, getBlockPos(), getBlockState());
            tardisId = TardisApi.idOfLevel(serverLevel).orElse(null);
            if (tardisId == null) return;
            DoorSyncSystem.register(tardisId, this);
            applyInteriorDoorVariant(serverLevel);
            DoorSyncSystem.pullFromExterior(serverLevel.getServer(), tardisId, this);
        }
        tickExit(serverLevel);
    }

    private void tickExit(ServerLevel serverLevel) {
        if (tardisId == null || !isLinked()) return;
        if (serverLevel.getGameTime() % EXIT_CHECK_INTERVAL_TICKS != 0) return;
        if (getDoorState().isClosed()) return;
        if (TardisPortalBridge.isActive(tardisId)) return;

        MinecraftServer server = serverLevel.getServer();
        Tardis tardis = TardisApi.get(server, tardisId).orElse(null);
        if (tardis == null || tardis.isTravelling()) return;

        for (ServerPlayer player : List.copyOf(serverLevel.players())) {
            if (player.isSpectator()) continue;
            boolean inZone = isInExitZone(player);
            if (TardisInteriorSystem.isArriving(player)) {
                if (!inZone && (isFlush() || frontDepth(player) > EXIT_FAR)) {
                    TardisInteriorSystem.clearArriving(player);
                }
                continue;
            }
            if (inZone) TardisInteriorSystem.exitInterior(server, player, tardisId);
        }
    }

    private double frontDepth(ServerPlayer player) {
        BlockState state = getBlockState();
        Direction facing = state.getValue(InteriorDoorBlock.FACING);
        Direction right = InteriorDoorBlock.rightDirectionOf(facing, InteriorDoorBlock.isFlipped(state));
        BlockPos bottom = InteriorDoorBlock.isFlipped(state)
                ? getBlockPos().below(InteriorDoorBlock.HEIGHT - 1) : getBlockPos();
        Vec3 centre = Vec3.atBottomCenterOf(bottom).add(right.getStepX() * 0.5, 0.0, right.getStepZ() * 0.5);
        return (player.getX() - centre.x) * facing.getStepX() + (player.getZ() - centre.z) * facing.getStepZ();
    }

    private boolean isInExitZone(ServerPlayer player) {
        BlockPos pos = getBlockPos();
        BlockState state = getBlockState();
        Direction facing = state.getValue(InteriorDoorBlock.FACING);

        if (InteriorDoorBlock.isFlush(state)) {
            Direction right = InteriorDoorBlock.rightDirectionOf(facing, false);
            Vec3 centre = Vec3.atBottomCenterOf(pos)
                    .add(right.getStepX() * 0.5 + facing.getStepX(), 0.0, right.getStepZ() * 0.5 + facing.getStepZ());
            double dx = player.getX() - centre.x;
            double dz = player.getZ() - centre.z;
            double lateral = dx * right.getStepX() + dz * right.getStepZ();
            double along = dx * facing.getStepX() + dz * facing.getStepZ();
            double eye = player.getEyeY() - pos.getY();
            return Math.abs(lateral) <= EXIT_HALF_WIDTH && Math.abs(along) <= 1.0 && eye >= 0.3 && eye <= 1.5;
        }

        boolean flipped = InteriorDoorBlock.isFlipped(state);
        Direction right = InteriorDoorBlock.rightDirectionOf(facing, flipped);
        BlockPos bottom = flipped ? pos.below(InteriorDoorBlock.HEIGHT - 1) : pos;
        Vec3 centre = Vec3.atBottomCenterOf(bottom).add(right.getStepX() * 0.5, 0.0, right.getStepZ() * 0.5);
        double dx = player.getX() - centre.x;
        double dz = player.getZ() - centre.z;
        double dy = player.getY() - centre.y;
        double depth = dx * facing.getStepX() + dz * facing.getStepZ();
        double lateral = dx * right.getStepX() + dz * right.getStepZ();
        return depth >= EXIT_NEAR && depth <= EXIT_FAR
                && Math.abs(lateral) <= EXIT_HALF_WIDTH
                && dy >= EXIT_BOTTOM && dy <= InteriorDoorBlock.HEIGHT;
    }

    private void applyInteriorDoorVariant(ServerLevel serverLevel) {
        if (tardisId == null || !storedVariantId().isEmpty()) return;
        matchTardis(serverLevel.getServer(), tardisId);
    }

    public void matchTardis(MinecraftServer server, UUID id) {
        if (!isLinked()) return;
        TardisApi.get(server, id).flatMap(Tardis::exterior)
                .map(exterior -> TardisDoorRegistry.interiorVariantForExterior(exterior.getExterior().id()))
                .ifPresent(this::setDoorVariant);
    }

    public void interact(boolean sneaking) {
        if (isTardisLocked() || isTardisSealedForTravel()) {
            announceLocked();
            return;
        }
        DoorSystem.InteractionResult result =
                DoorSystem.interact(asEntity(), sneaking, System.currentTimeMillis());
        if (result.hasNoAnimation()) return;
        if (controller != null) {
            controller.setAnimationSpeed(1.0);
        }
        playDoorAnimation(result.animationName());
        playDoorSound(result.opening());
        sync();

        if (tardisId != null && level instanceof ServerLevel serverLevel) {
            DoorSyncSystem.onInteriorDoorChanged(serverLevel.getServer(), tardisId, this,
                    result.state(), result.animationName(), result.opening());
        }
    }

    public void applySyncedDoor(DoorState state, String animation, boolean opening) {
        DoorSystem.applyRemoteDoorState(asEntity(), state, opening, System.currentTimeMillis());
        if (controller != null) {
            controller.setAnimationSpeed(1.0);
        }
        playDoorAnimation(animation);
        playDoorSound(opening);
        sync();
    }

    private Optional<Tardis> tardisApi() {
        return level instanceof ServerLevel serverLevel
                ? TardisApi.get(serverLevel.getServer(), tardisId)
                : Optional.empty();
    }

    private boolean isTardisLocked() {
        return tardisApi().map(Tardis::isLocked).orElse(false);
    }

    private boolean isTardisSealedForTravel() {
        return tardisApi().map(Tardis::areDoorsSealedForTravel).orElse(false);
    }

    public void announceLocked() {
        long now = System.currentTimeMillis();
        if (now - lastLockedFeedbackAt < 1000L) return;
        lastLockedFeedbackAt = now;
        triggerAnim(LOCK_CONTROLLER, "is_locked");
        assert level != null;
        level.playSound(null, getBlockPos(), GCSounds.TARDIS_IS_LOCKED, SoundSource.BLOCKS, 1.0f, 1.0f);
        if (level != null) {
            level.playSound(null, soundPos(), GCSounds.TARDIS_IS_LOCKED, SoundSource.BLOCKS, 1.0f, 1.0f);
        }
    }

    public void applySyncedLock(boolean locked) {
        playLockSound(locked);
    }

    public void snapTo(DoorState state) {
        if (getDoorState() == state) return;
        DoorSystem.snapToState(asEntity(), state);
        playDoorAnimation(animationFor(state));
        sync();
    }

    private void playDoorAnimation(String animationName) {
        if (level != null && level.isClientSide()) {
            triggerAnim("anim_controller", animationName);
            return;
        }
        doorAnimSeq++;
        doorAnim = animationName;
    }

    private static String animationFor(DoorState state) {
        return switch (state) {
            case CLOSED -> "close_all";
            case LEFT_OPEN -> "open_left";
            case RIGHT_OPEN -> "open_right";
            case BOTH_OPEN -> "open_all";
        };
    }

    private BlockPos soundPos() {
        if (level instanceof ServerLevel serverLevel) {
            UUID id = tardisId != null ? tardisId : TardisApi.idOfLevel(serverLevel).orElse(null);
            return TardisApi.get(serverLevel.getServer(), id).flatMap(Tardis::consolePos).orElse(getBlockPos());
        }
        return getBlockPos();
    }

    private void playDoorSound(boolean opening) {
        if (level == null) return;
        SoundEvent sound = opening ? GCSounds.TARDIS_DOOR_OPEN : GCSounds.TARDIS_DOOR_CLOSE;
        level.playSound(null, getBlockPos(), sound, SoundSource.BLOCKS, 1.0f, 1.0f);
        level.playSound(null, soundPos(), sound, SoundSource.BLOCKS, 1.0f, 1.0f);
    }

    private void playLockSound(boolean locked) {
        if (level == null) return;
        SoundEvent sound = locked ? GCSounds.TARDIS_DOOR_LOCK : GCSounds.TARDIS_DOOR_UNLOCK;
        level.playSound(null, getBlockPos(), sound, SoundSource.BLOCKS, 1.0f, 1.0f);
        level.playSound(null, soundPos(), sound, SoundSource.BLOCKS, 1.0f, 1.0f);
    }

    private void snapAnimationClient(AnimationController<InteriorDoorBlockEntity> c) {
        c.setAnimation(RawAnimation.begin().thenPlay(animationFor(getDoorState())));
        c.setAnimationSpeed(999999);
        new Thread(() -> {
            try {
                Thread.sleep(50);
                if (controller != null) {
                    controller.setAnimationSpeed(1.0);
                }
            } catch (InterruptedException ignored) {}
        }).start();
    }

    public void sync() {
        if (level != null) {
            setChanged();
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(@NonNull ValueOutput output) {
        super.saveAdditional(output);
        components.save(output, SAVED);
        output.putInt("DoorAnimSeq", doorAnimSeq);
        output.putString("DoorAnim", doorAnim);
    }

    @Override
    public void loadAdditional(@NonNull ValueInput input) {
        super.loadAdditional(input);
        components.load(input, SAVED);
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
    public @NonNull CompoundTag getUpdateTag(HolderLookup.@NonNull Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        controller = new AnimationController<>("anim_controller", 0, state -> {
            if (needsStateSync && level != null && level.isClientSide()) {
                snapAnimationClient(state.controller());
                needsStateSync = false;
            }
            return PlayState.CONTINUE;
        });
        controller.triggerableAnim("open_left", RawAnimation.begin().thenPlay("open_left"))
                .triggerableAnim("open_right", RawAnimation.begin().thenPlay("open_right"))
                .triggerableAnim("open_all", RawAnimation.begin().thenPlay("open_all"))
                .triggerableAnim("close_left", RawAnimation.begin().thenPlay("close_left"))
                .triggerableAnim("close_right", RawAnimation.begin().thenPlay("close_right"))
                .triggerableAnim("close_all", RawAnimation.begin().thenPlay("close_all"));
        registrar.add(controller);

        AnimationController<InteriorDoorBlockEntity> lockController =
                new AnimationController<>(LOCK_CONTROLLER, 0, _ -> PlayState.CONTINUE);
        lockController.triggerableAnim("is_locked", RawAnimation.begin().thenPlay("is_locked"));
        registrar.add(lockController);
    }

    @Override
    public @NotNull AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}

package com.noahtnt2009.gallifreyan_chronicles.block.entity;

import com.geckolib.animatable.GeoBlockEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.util.GeckoLibUtil;
import com.noahtnt2009.gallifreyan_chronicles.block.InteriorDoorBlock;
import com.noahtnt2009.gallifreyan_chronicles.ecs.ComponentHolder;
import com.noahtnt2009.gallifreyan_chronicles.ecs.ComponentStore;
import com.noahtnt2009.gallifreyan_chronicles.ecs.ComponentType;
import com.noahtnt2009.gallifreyan_chronicles.ecs.Entity;
import com.noahtnt2009.gallifreyan_chronicles.init.GCSounds;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.TardisComponentTypes;
import com.noahtnt2009.gallifreyan_chronicles.tardis.door.TardisDoorRegistry;
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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
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
        return ((InteriorDoorBlock) getBlockState().getBlock()).variant();
    }

    public String getDoorVariantId() {
        String fixed = ((InteriorDoorBlock) getBlockState().getBlock()).fixedVariantId();
        if (!fixed.isEmpty()) return fixed;
        return asEntity().get(TardisComponentTypes.DOOR_VARIANT).variantId();
    }

    public void setDoorVariant(String variantId) {
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
        if (pulledFromExterior || !(level instanceof ServerLevel serverLevel)) return;
        pulledFromExterior = true;
        if (!isLinked()) return;
        tardisId = DoorSyncSystem.tardisIdOf(serverLevel);
        if (tardisId == null) return;
        DoorSyncSystem.register(tardisId, this);
        applyInteriorDoorVariant(serverLevel);
        DoorSyncSystem.pullFromExterior(serverLevel.getServer(), tardisId, this);
    }

    private void applyInteriorDoorVariant(ServerLevel serverLevel) {
        if (tardisId == null || !getDoorVariantId().isEmpty()) return;
        TardisInterior interior = TardisInteriorSystem.getInteriorDefinition(serverLevel.getServer(), tardisId);
        interior.doorVariant().ifPresent(this::setDoorVariant);
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
        triggerAnim("anim_controller", result.animationName());
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
        triggerAnim("anim_controller", animation);
        playDoorSound(opening);
        sync();
    }

    private boolean isTardisLocked() {
        if (tardisId == null || !(level instanceof ServerLevel serverLevel)) return false;
        TardisExteriorBlockEntity exterior = DoorSyncSystem.findExterior(serverLevel.getServer(), tardisId);
        return exterior != null && exterior.isLocked();
    }

    private boolean isTardisSealedForTravel() {
        if (tardisId == null || !(level instanceof ServerLevel serverLevel)) return false;
        TardisExteriorBlockEntity exterior = DoorSyncSystem.findExterior(serverLevel.getServer(), tardisId);
        return exterior != null && exterior.areDoorsSealedForTravel();
    }

    public void announceLocked() {
        long now = System.currentTimeMillis();
        if (now - lastLockedFeedbackAt < 1000L) return;
        lastLockedFeedbackAt = now;
        triggerAnim(LOCK_CONTROLLER, "is_locked");
        if (level != null) {
            level.playSound(null, getBlockPos(), GCSounds.TARDIS_IS_LOCKED, SoundSource.BLOCKS, 1.0f, 1.0f);
        }
    }

    public void applySyncedLock(boolean locked) {
        playLockSound(locked);
    }

    public void snapTo(DoorState state) {
        if (getDoorState() == state) return;
        DoorSystem.snapToState(asEntity(), state);
        triggerAnim("anim_controller", animationFor(state));
        sync();
    }

    private static String animationFor(DoorState state) {
        return switch (state) {
            case CLOSED -> "close_all";
            case LEFT_OPEN -> "open_left";
            case RIGHT_OPEN -> "open_right";
            case BOTH_OPEN -> "open_all";
        };
    }

    private void playDoorSound(boolean opening) {
        if (level == null) return;
        SoundEvent sound = opening ? GCSounds.TARDIS_DOOR_OPEN : GCSounds.TARDIS_DOOR_CLOSE;
        level.playSound(null, getBlockPos(), sound, SoundSource.BLOCKS, 1.0f, 1.0f);
    }

    private void playLockSound(boolean locked) {
        if (level == null) return;
        SoundEvent sound = locked ? GCSounds.TARDIS_DOOR_LOCK : GCSounds.TARDIS_DOOR_UNLOCK;
        level.playSound(null, getBlockPos(), sound, SoundSource.BLOCKS, 1.0f, 1.0f);
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
    }

    @Override
    public void loadAdditional(@NonNull ValueInput input) {
        super.loadAdditional(input);
        components.load(input, SAVED);
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

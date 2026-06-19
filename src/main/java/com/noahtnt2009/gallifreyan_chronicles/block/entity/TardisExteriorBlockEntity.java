package com.noahtnt2009.gallifreyan_chronicles.block.entity;

import com.geckolib.animatable.GeoBlockEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.util.GeckoLibUtil;
import com.noahtnt2009.gallifreyan_chronicles.ecs.Entity;
import com.noahtnt2009.gallifreyan_chronicles.ecs.component.ComponentTypes;
import com.noahtnt2009.gallifreyan_chronicles.ecs.component.DoorState;
import com.noahtnt2009.gallifreyan_chronicles.ecs.component.TransformComponent;
import com.noahtnt2009.gallifreyan_chronicles.ecs.system.DoorSystem;
import com.noahtnt2009.gallifreyan_chronicles.ecs.system.ExteriorSystem;
import com.noahtnt2009.gallifreyan_chronicles.ecs.system.TardisLinkSystem;
import com.noahtnt2009.gallifreyan_chronicles.registry.GCBlockEntities;
import com.noahtnt2009.gallifreyan_chronicles.registry.GCSounds;
import com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.TardisExterior;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

public class TardisExteriorBlockEntity extends BlockEntity implements GeoBlockEntity {

    private AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public TardisExteriorBlockEntity(BlockPos pos, BlockState state) {
        super(GCBlockEntities.TARDIS_EXTERIOR_BLOCK_ENTITY_TYPE, pos, state);
    }

    // ---------------- Entity wrapper ----------------

    public Entity asEntity() {
        return Entity.of(this, this::sync);
    }

    @Override
    public void setLevel(@NonNull Level level) {
        super.setLevel(level);

        if (cache == null) {
            cache = GeckoLibUtil.createInstanceCache(this);
        }

        // Only run on CLIENT (this is visual only!)
        if (level.isClientSide()) {
            syncVisualState();
        }
    }

    // ---------------- Exterior ----------------

    public TardisExterior getExterior() {
        return ExteriorSystem.exteriorOf(asEntity());
    }

    public void setExterior(TardisExterior exterior) {
        ExteriorSystem.setExterior(asEntity(), exterior);
        resetAnimationState();
    }

    private void syncVisualState() {
        if (level == null || !level.isClientSide()) return;

        DoorState state = getDoorState();

        switch (state) {

            case CLOSED -> {
                triggerAnim("anim_controller", "open_all");
            }

            case LEFT_OPEN -> {
                triggerAnim("anim_controller", "open_right");
            }

            case RIGHT_OPEN -> {
                triggerAnim("anim_controller", "open_left");
            }

            case BOTH_OPEN -> {
                triggerAnim("anim_controller", "close_all");
            }
        }
    }

    /**
     * ONLY resets GeckoLib cache.
     * DO NOT touch ECS state here.
     */
    private void resetAnimationState() {
        cache = GeckoLibUtil.createInstanceCache(this);

        // IMPORTANT: restore visuals after reset
        if (level != null && !level.isClientSide()) {
            reapplyDoorVisualState();
        }

        sync();
    }

    /**
     * Re-applies ECS door state to GeckoLib animations.
     * This fixes reload + cache reset desync.
     */
    private void reapplyDoorVisualState() {
        if (level != null && level.isClientSide()) return;

        DoorState state = getDoorState();

        switch (state) {
            case LEFT_OPEN -> playAnimation("open_left");
            case RIGHT_OPEN -> playAnimation("open_right");
            case BOTH_OPEN -> playAnimation("open_all");
            case CLOSED -> playAnimation("close_all");
        }
    }

    // ---------------- Yaw ----------------

    public float getYaw() {
        return asEntity().get(ComponentTypes.TRANSFORM).yaw();
    }

    public void setYaw(float yaw) {
        asEntity().set(ComponentTypes.TRANSFORM, new TransformComponent(yaw));
    }

    // ---------------- TARDIS link ----------------

    public @Nullable UUID getTardisId() {
        return TardisLinkSystem.linkedTardisId(asEntity()).orElse(null);
    }

    // ---------------- Door ----------------

    public DoorState getDoorState() {
        return DoorSystem.stateOf(asEntity());
    }

    public void interact(boolean sneaking) {
        DoorSystem.InteractionResult result =
                DoorSystem.interact(asEntity(), sneaking, System.currentTimeMillis());

        if (result.playedNothing()) return;

        playAnimation(result.animationName());
        playDoorSound(result.opening());
        sync();
    }

    private void playDoorSound(boolean opening) {
        if (level == null || level.isClientSide()) return;

        BlockPos pos = getBlockPos();
        level.playSound(
                null,
                pos.getX() + 0.5,
                pos.getY() + 0.5,
                pos.getZ() + 0.5,
                opening ? GCSounds.POLICE_BOX_OPEN : GCSounds.POLICE_BOX_CLOSE,
                SoundSource.BLOCKS,
                1.0f,
                1.0f
        );
    }

    public void playAnimation(String animationName) {
        triggerAnim("anim_controller", animationName);
    }

    // ---------------- Sync ----------------

    public void sync() {
        if (level != null) {
            setChanged();
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    // ---------------- GeckoLib ----------------

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>("anim_controller", 0, state -> PlayState.CONTINUE)
                .triggerableAnim("open_left",   RawAnimation.begin().thenPlay("open_left"))
                .triggerableAnim("open_right",  RawAnimation.begin().thenPlay("open_right"))
                .triggerableAnim("open_all",    RawAnimation.begin().thenPlay("open_all"))
                .triggerableAnim("close_left",  RawAnimation.begin().thenPlay("close_left"))
                .triggerableAnim("close_right", RawAnimation.begin().thenPlay("close_right"))
                .triggerableAnim("close_left_shift",  RawAnimation.begin().thenPlay("close_left_shift"))
                .triggerableAnim("close_right_shift", RawAnimation.begin().thenPlay("close_right_shift"))
                .triggerableAnim("close_all",   RawAnimation.begin().thenPlay("close_all"))
        );
    }

    @Override
    public @NotNull AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
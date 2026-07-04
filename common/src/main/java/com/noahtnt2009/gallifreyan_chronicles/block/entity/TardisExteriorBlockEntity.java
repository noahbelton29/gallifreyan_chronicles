package com.noahtnt2009.gallifreyan_chronicles.block.entity;

import com.geckolib.animatable.GeoBlockEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.util.GeckoLibUtil;
import com.noahtnt2009.gallifreyan_chronicles.ecs.ComponentHolder;
import com.noahtnt2009.gallifreyan_chronicles.ecs.ComponentStore;
import com.noahtnt2009.gallifreyan_chronicles.ecs.Entity;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.component.DoorComponent;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.component.DoorState;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.TardisComponentTypes;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.component.TransformComponent;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.system.DoorSystem;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.system.ExteriorSystem;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.system.GlowSystem;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.system.TardisLinkSystem;
import com.noahtnt2009.gallifreyan_chronicles.init.GCGameRules;
import com.noahtnt2009.gallifreyan_chronicles.init.GCSounds;
import com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.TardisExterior;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.UUID;
import java.util.function.Supplier;

public class TardisExteriorBlockEntity extends BlockEntity implements GeoBlockEntity, ComponentHolder {
    public static Supplier<BlockEntityType<TardisExteriorBlockEntity>> TYPE;
    private AnimationController<TardisExteriorBlockEntity> controller;
    private final ComponentStore components = new ComponentStore();
    private AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private boolean needsStateSync = false;

    public TardisExteriorBlockEntity(BlockPos pos, BlockState state) {
        super(TYPE.get(), pos, state);
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

    private void snapToState() {
        if (level == null || !level.isClientSide() || controller == null) return;

        DoorState state = getDoorState();
        String animName = getAnimationForDoorState(state);

        System.out.println("Snapping to: " + state + " -> " + animName);

        controller.setAnimation(RawAnimation.begin().thenPlay(animName));
        controller.setAnimationSpeed(999999);
        scheduleSpeedReset();
    }

    private void scheduleSpeedReset() {
        if (level != null && level.isClientSide()) {
            Minecraft.getInstance().execute(() -> {
                new Thread(() -> {
                    try {
                        Thread.sleep(50);
                        if (controller != null) {
                            controller.setAnimationSpeed(1.0);
                            System.out.println("Reset animation speed to normal");
                        }
                    } catch (InterruptedException ignored) {}
                }).start();
            });
        }
    }

    private void resetAnimationState() {
        cache = GeckoLibUtil.createInstanceCache(this);

        if (level != null && !level.isClientSide()) {
            reapplyDoorVisualState();
        }

        sync();
    }

    private void reapplyDoorVisualState() {
        if (level != null && level.isClientSide()) return;

        DoorState state = getDoorState();
        String animName = getAnimationForDoorState(state);

        System.out.println("Reapplying door state: " + state + " -> " + animName);

        playAnimation(animName);
    }

    public float getYaw() {
        return asEntity().get(TardisComponentTypes.TRANSFORM).yaw();
    }
    public void setYaw(float yaw) {
        asEntity().set(TardisComponentTypes.TRANSFORM, new TransformComponent(yaw));
    }

    public @Nullable UUID getTardisId() {
        return TardisLinkSystem.linkedTardisId(asEntity()).orElse(null);
    }

    public DoorState getDoorState() {
        return DoorSystem.stateOf(asEntity());
    }

    public boolean isGlowing() {
        return asEntity().get(TardisComponentTypes.GLOW).glowing();
    }

    public void setGlowing(boolean glowing) {
        GlowSystem.setManual(asEntity(), glowing);
        applyGlowToBlockState(glowing);
        sync();
    }

    private void applyGlowToBlockState(boolean glowing) {
        if (level == null || level.isClientSide()) return;
        BlockState current = getBlockState();
        if (current.getValue(com.noahtnt2009.gallifreyan_chronicles.block.TardisBlock.GLOWING) != glowing) {
            level.setBlock(getBlockPos(), current.setValue(com.noahtnt2009.gallifreyan_chronicles.block.TardisBlock.GLOWING, glowing), 3);
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TardisExteriorBlockEntity blockEntity) {
        if (level.isClientSide()) return;

        MinecraftServer server = level.getServer();
        boolean daylightAffectsGlow = server == null
                || server.getGameRules().get(GCGameRules.DAYLIGHT_CYCLE_AFFECTS_GLOW);

        if (GlowSystem.tick(blockEntity.asEntity(), level.getDefaultClockTime(), daylightAffectsGlow)) {
            blockEntity.applyGlowToBlockState(blockEntity.isGlowing());
            blockEntity.sync();
        }
    }

    public void setDoorState(DoorState newState) {
        if (level == null || level.isClientSide()) {
            DoorComponent current = asEntity().get(TardisComponentTypes.DOOR);
            asEntity().set(TardisComponentTypes.DOOR, current.withState(newState));
            resetAnimationState();
            sync();
            return;
        }
        asEntity().set(TardisComponentTypes.DOOR, DoorComponent.closed().withState(newState)); // or proper
        sync();
    }

    public void interact(boolean sneaking) {
        DoorSystem.InteractionResult result =
                DoorSystem.interact(asEntity(), sneaking, System.currentTimeMillis());

        if (result.playedNothing()) return;

        if (controller != null) {
            controller.setAnimationSpeed(1.0);
        }

        playAnimation(result.animationName());
        playDoorSound(result.opening());
        sync();
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
                net.minecraft.sounds.SoundSource.BLOCKS,
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
    }

    @Override
    public void loadAdditional(@NonNull ValueInput input) {
        super.loadAdditional(input);
        components.load(input, TardisComponentTypes.ALL);
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
                                    System.out.println("Reset animation speed to normal");
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

        if (level != null && level.isClientSide()) {
            snapToState();
        }
    }

    @Override
    public @NotNull AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
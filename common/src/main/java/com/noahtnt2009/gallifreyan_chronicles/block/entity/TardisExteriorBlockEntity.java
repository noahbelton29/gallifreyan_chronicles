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
import com.noahtnt2009.gallifreyan_chronicles.ecs.ComponentStore;
import com.noahtnt2009.gallifreyan_chronicles.ecs.Entity;
import com.noahtnt2009.gallifreyan_chronicles.entity.TardisKeyEntity;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.TardisLinkable;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.component.DoorState;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.TardisComponentTypes;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.component.TransformComponent;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.system.DoorSystem;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.system.ExteriorSystem;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.system.GlowSystem;
import com.noahtnt2009.gallifreyan_chronicles.init.GCGameRules;
import com.noahtnt2009.gallifreyan_chronicles.init.GCSounds;
import com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.TardisExterior;
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
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import java.util.UUID;
import java.util.function.Supplier;

public class TardisExteriorBlockEntity extends BlockEntity implements GeoBlockEntity, ComponentHolder, TardisLinkable,
        TardisExteriorKeyLayer.TardisExteriorRenderable {
    public static Supplier<BlockEntityType<TardisExteriorBlockEntity>> TYPE;
    private long lastLockedFeedbackAt = 0L;
    private AnimationController<TardisExteriorBlockEntity> controller;
    private boolean locked = true;
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
        Vec3 worldPos = Vec3.atCenterOf(getBlockPos());
        TardisExterior exterior = getExterior();
        Vec3 keyOffset = exterior != null
                ? exterior.worldKeyOffset(getYaw())
                : TardisKeyEntity.DEFAULT_OFFSET;
        TardisKeyEntity entity =
                TardisKeyEntity.create(serverLevel, getBlockPos(), worldPos, keyOffset);
        serverLevel.addFreshEntity(entity);
        this.keyEntity = entity;
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

    public boolean isLocked() {
        return locked;
    }

    public void toggleKeyLockAnimation() {
        if (!hasKeyRendered()) {
            return;
        }
        boolean nowLocked = !locked;
        String animName = nowLocked ? "key_lock" : "key_unlock";
        locked = nowLocked;
        triggerAnim("key_controller", animName);
        playKeySound();
        sync();
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
        this.locked = true;
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
        GlowSystem.setManual(asEntity(), glowing);
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
        MinecraftServer server = level.getServer();
        boolean daylightAffectsGlow = server == null
                || server.getGameRules().get(GCGameRules.DAYLIGHT_CYCLE_AFFECTS_GLOW);
        if (GlowSystem.tick(blockEntity.asEntity(), level.getDefaultClockTime(), daylightAffectsGlow)) {
            blockEntity.applyGlowToBlockState(blockEntity.isGlowing());
            blockEntity.sync();
        }
    }

    public void interact(boolean sneaking) {
        if (isLocked()) {
            return;
        }
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

        UUID ownerId = tardis.get().getOwnerId();
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
    }

    @Override
    public void loadAdditional(@NonNull ValueInput input) {
        super.loadAdditional(input);
        components.load(input, TardisComponentTypes.ALL);
        input.getString("RenderedKeyOwner").ifPresent(s -> renderedKeyOwner = UUID.fromString(s));
        heldKeyStack = input.read("HeldKeyStack", ItemStack.OPTIONAL_CODEC)
                .orElse(ItemStack.EMPTY);
        locked = input.getBooleanOr("Locked", true);
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
                .triggerableAnim("is_locked", RawAnimation.begin().thenPlay("is_locked"));
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

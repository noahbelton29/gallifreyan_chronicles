package uk.nbforge.gallifreyan_chronicles.core.block_entity;

import com.geckolib.animatable.GeoBlockEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.util.GeckoLibUtil;
import com.mojang.serialization.Codec;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import uk.nbforge.gallifreyan_chronicles.core.block.InteriorDoorBlock;
import uk.nbforge.gallifreyan_chronicles.core.data_registry.TardisDoorRegistry;
import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import uk.nbforge.gallifreyan_chronicles.tardis.door.DoorMotion;
import uk.nbforge.gallifreyan_chronicles.tardis.door.DoorState;
import uk.nbforge.gallifreyan_chronicles.tardis.door.DoorStatus;
import uk.nbforge.gallifreyan_chronicles.tardis.door.DoorSync;
import uk.nbforge.gallifreyan_chronicles.tardis.door.InteriorDoorExitZone;
import uk.nbforge.gallifreyan_chronicles.tardis.door.InteriorDoorSounds;
import uk.nbforge.gallifreyan_chronicles.tardis.door.TardisDoorVariant;
import uk.nbforge.gallifreyan_chronicles.util.BlockEntitySyncUtil;
import uk.nbforge.gallifreyan_chronicles.util.SavedValue;

public class InteriorDoorBlockEntity extends BlockEntity implements GeoBlockEntity {
    public static Supplier<BlockEntityType<InteriorDoorBlockEntity>> TYPE;

    private static final String ANIM_CONTROLLER = "anim_controller";
    private static final String LOCK_CONTROLLER = "lock_controller";
    private static final int SNAP_SPEED = 999999;
    private static final int SNAP_FRAMES = 3;
    private static final int CONTROL_SYNC_INTERVAL_TICKS = 10;

    // TARDIS_DOOR leads outside and follows the exterior lock and flight seal, PRIMARY is the one the console's
    // interior door control drives, DECORATIVE is anything else
    public enum Role {
        TARDIS_DOOR,
        PRIMARY,
        DECORATIVE
    }

    private final SavedValue<DoorStatus> door = new SavedValue<>("door", DoorStatus.CODEC, DoorStatus::closed);
    private final SavedValue<String> doorVariant = new SavedValue<>(
            "door_variant", Codec.STRING.optionalFieldOf("variant_id", "").codec(), () -> "");
    private boolean primary;
    private String roundelBlock = "";

    private final InteriorDoorExitZone exitZone = new InteriorDoorExitZone(this);
    private final InteriorDoorSounds sounds = new InteriorDoorSounds(this);

    private @Nullable UUID tardisId = null;
    private boolean pulledFromExterior = false;
    private long cooldownUntilTick = 0L;
    private long lastLockedFeedbackAt = 0L;

    private int doorAnimSeq = 0;
    private String doorAnim = "";
    private boolean doorAnimSeqKnown = false;
    private boolean doorAnimInstant = false;
    private int snapFramesLeft = 0;
    private boolean needsStateSync = false;

    private AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private @Nullable AnimationController<InteriorDoorBlockEntity> controller;

    public InteriorDoorBlockEntity(BlockPos pos, BlockState state) {
        super(TYPE.get(), pos, state);
    }

    public void serverTick() {
        if (!(level instanceof ServerLevel serverLevel)) return;
        if (!pulledFromExterior) {
            pulledFromExterior = true;
            if (!isLinked()) {
                if (!primary) return;
                tardisId = Tardis.idOfLevel(serverLevel).orElse(null);
                if (tardisId != null) DoorSync.register(tardisId, this);
                return;
            }
            InteriorDoorBlock.placeBarriers(serverLevel, getBlockPos(), getBlockState());
            tardisId = Tardis.idOfLevel(serverLevel).orElse(null);
            if (tardisId == null) return;
            DoorSync.register(tardisId, this);
            Tardis.get(serverLevel.getServer(), tardisId).ifPresent(tardis -> {
                if (storedVariantId().isEmpty()) matchTardis(tardis);
                if (!primary) DoorSync.pullFromExterior(tardis, this);
            });
        }
        if (primary && tardisId != null && serverLevel.getGameTime() % CONTROL_SYNC_INTERVAL_TICKS == 0) {
            Tardis.get(serverLevel.getServer(), tardisId)
                    .ifPresent(tardis -> DoorSync.reconcileInteriorDoorControl(tardis, getDoorState()));
        }
        exitZone.tick(serverLevel);
    }

    public @Nullable UUID getTardisId() {
        return tardisId;
    }

    public Role role() {
        if (primary) return Role.PRIMARY;
        return isLinked() ? Role.TARDIS_DOOR : Role.DECORATIVE;
    }

    public boolean followsTardisLock() {
        return role() == Role.TARDIS_DOOR;
    }

    public boolean isPrimary() {
        return primary;
    }

    public void setPrimary(boolean primary) {
        if (this.primary == primary) return;
        this.primary = primary;
        sync();
    }

    public boolean isLinked() {
        return block().isLinked();
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

    private InteriorDoorBlock block() {
        return (InteriorDoorBlock) getBlockState().getBlock();
    }

    // the model, a linked door takes it from its TARDIS's door style
    public String getVariant() {
        if (isLinked()) {
            TardisDoorVariant variant = TardisDoorRegistry.resolveInterior(storedVariantId());
            if (variant != null) return TardisDoorRegistry.localId(variant.parent());
        }
        return block().variant();
    }

    // the style (texture, sounds), a style the block forces wins
    public String getDoorVariantId() {
        String fixed = block().fixedVariantId();
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
            doorVariant.set(resolved);
            sync();
            return;
        }
        String model = getVariant();
        String resolved = TardisDoorRegistry.hasVariant(model, variantId)
                ? variantId
                : TardisDoorRegistry.defaultVariantId(model);
        doorVariant.set(resolved);
        sync();
    }

    public void matchTardis(Tardis tardis) {
        if (!isLinked()) return;
        tardis.exterior()
                .map(exterior -> TardisDoorRegistry.interiorVariantForExterior(
                        exterior.getExterior().id()))
                .ifPresent(this::setDoorVariant);
    }

    public String getTextureId() {
        return TardisDoorRegistry.textureIdFor(getVariant(), getDoorVariantId());
    }

    public String getRoundelBlock() {
        return roundelBlock;
    }

    public boolean rejectsRoundel() {
        return false;
    }

    public void setRoundelBlock(String blockId) {
        if (this.roundelBlock.equals(blockId)) return;
        this.roundelBlock = blockId;
        sync();
    }

    private String storedVariantId() {
        return doorVariant.get();
    }

    private @Nullable TardisDoorVariant resolvedVariant() {
        return TardisDoorRegistry.resolve(getVariant(), getDoorVariantId());
    }

    private boolean openAllOnly() {
        TardisDoorVariant variant = resolvedVariant();
        return variant != null && variant.openAllOnly();
    }

    public DoorState getDoorState() {
        return door.get().state();
    }

    public void interact(boolean sneaking) {
        if (isOnCooldown()) return;
        if (followsTardisLock() && isTardisLocked()) {
            announceLocked();
            return;
        }
        DoorMotion.InteractionResult result =
                DoorMotion.interact(door.get(), sneaking, System.currentTimeMillis(), openAllOnly());
        if (result.next() != null) {
            door.set(result.next());
            sync();
        }
        if (result.hasNoAnimation()) return;
        startCooldown();
        if (controller != null) {
            controller.setAnimationSpeed(1.0);
        }
        playDoorAnimation(result.animationName());
        sounds.door(result.opening());
        sync();

        if (tardisId != null && level instanceof ServerLevel serverLevel) {
            Tardis tardis = Tardis.get(serverLevel.getServer(), tardisId).orElse(null);
            if (tardis == null) return;
            if (primary) {
                DoorSync.syncInteriorDoorControl(tardis, result.state());
            } else {
                DoorSync.onInteriorDoorChanged(tardis, this, result.state(), result.animationName(), result.opening());
            }
        }
    }

    public void applySyncedDoor(DoorState state, String animation, boolean opening) {
        if (primary) return;
        if (openAllOnly()) {
            boolean open = !state.isClosed();
            if (open != getDoorState().isClosed()) return;
            state = open ? DoorState.BOTH_OPEN : DoorState.CLOSED;
            animation = open ? "open_all" : "close_all";
            opening = open;
        }
        door.set(DoorMotion.applyRemoteDoorState(door.get(), state, opening, System.currentTimeMillis()));
        sync();
        if (controller != null) {
            controller.setAnimationSpeed(1.0);
        }
        playDoorAnimation(animation);
        sounds.door(opening);
        sync();
    }

    public void snapTo(DoorState state) {
        if (openAllOnly() && !state.isClosed()) state = DoorState.BOTH_OPEN;
        if (getDoorState() == state) return;
        door.set(DoorMotion.snapToState(door.get(), state));
        sync();
        playDoorAnimation(animationFor(state));
        doorAnimInstant = true;
        sync();
    }

    public boolean isOnCooldown() {
        return primary && level != null && level.getGameTime() < cooldownUntilTick;
    }

    private void startCooldown() {
        if (!primary || level == null) return;
        TardisDoorVariant variant = resolvedVariant();
        float seconds = variant != null ? variant.cooldownSeconds() : TardisDoorVariant.DEFAULT_COOLDOWN_SECONDS;
        cooldownUntilTick = level.getGameTime() + Math.max(0, Math.round(seconds * 20f));
    }

    public boolean isTardisLocked() {
        if (!(level instanceof ServerLevel serverLevel)) return false;
        return Tardis.get(serverLevel.getServer(), tardisId)
                .map(tardis -> tardis.isLocked()
                        || tardis.exterior()
                                .map(exterior -> exterior.lock().hasKeyRendered())
                                .orElse(false))
                .orElse(false);
    }

    public void announceLocked() {
        if (!followsTardisLock()) return;
        long now = System.currentTimeMillis();
        if (now - lastLockedFeedbackAt < 1000L) return;
        lastLockedFeedbackAt = now;
        triggerAnim(LOCK_CONTROLLER, "is_locked");
        sounds.locked();
    }

    public void applySyncedLock(boolean locked) {
        if (!followsTardisLock()) return;
        sounds.lock(locked);
    }

    // server bumps the seq so clients play it once in loadAdditional
    private void playDoorAnimation(String animationName) {
        if (level != null && level.isClientSide()) {
            triggerAnim(ANIM_CONTROLLER, animationName);
            return;
        }
        doorAnimSeq++;
        doorAnim = animationName;
        doorAnimInstant = false;
    }

    private static String animationFor(DoorState state) {
        return switch (state) {
            case CLOSED -> "close_all";
            case LEFT_OPEN -> "open_left";
            case RIGHT_OPEN -> "open_right";
            case BOTH_OPEN -> "open_all";
        };
    }

    // "snapping" is just playing the anim stupidly fast for a few frames
    private void snapAnimationClient(AnimationController<InteriorDoorBlockEntity> c) {
        c.setAnimation(RawAnimation.begin().thenPlay(animationFor(getDoorState())));
        c.setAnimationSpeed(SNAP_SPEED);
        snapFramesLeft = SNAP_FRAMES;
    }

    private void startSnapClient() {
        if (controller == null) return;
        controller.setAnimationSpeed(SNAP_SPEED);
        snapFramesLeft = SNAP_FRAMES;
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
            DoorSync.unregister(tardisId, this);
        }
    }

    public void sync() {
        BlockEntitySyncUtil.sendToClients(this);
    }

    @Override
    protected void saveAdditional(@NonNull ValueOutput output) {
        super.saveAdditional(output);
        door.save(output);
        doorVariant.save(output);
        output.putBoolean("Primary", primary);
        output.putString("RoundelBlock", roundelBlock);
        output.putInt("DoorAnimSeq", doorAnimSeq);
        output.putString("DoorAnim", doorAnim);
        output.putBoolean("DoorAnimInstant", doorAnimInstant);
    }

    @Override
    public void loadAdditional(@NonNull ValueInput input) {
        super.loadAdditional(input);
        door.load(input);
        doorVariant.load(input);
        primary = input.getBooleanOr("Primary", false);
        roundelBlock = input.getStringOr("RoundelBlock", "");

        int animSeq = input.getIntOr("DoorAnimSeq", 0);
        String anim = input.getStringOr("DoorAnim", "");
        boolean instant = input.getBooleanOr("DoorAnimInstant", false);
        if (doorAnimSeqKnown && animSeq != doorAnimSeq && !anim.isEmpty() && level != null && level.isClientSide()) {
            if (instant) {
                startSnapClient();
            } else if (controller != null) {
                controller.setAnimationSpeed(1.0);
            }
            triggerAnim(ANIM_CONTROLLER, anim);
        }
        doorAnimSeq = animSeq;
        doorAnim = anim;
        doorAnimInstant = instant;
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
        controller = new AnimationController<>(ANIM_CONTROLLER, 0, state -> {
            if (needsStateSync && level != null && level.isClientSide()) {
                snapAnimationClient(state.controller());
                needsStateSync = false;
            } else if (snapFramesLeft > 0 && --snapFramesLeft == 0) {
                state.controller().setAnimationSpeed(1.0);
            }
            return PlayState.CONTINUE;
        });
        controller
                .triggerableAnim("open_left", RawAnimation.begin().thenPlay("open_left"))
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
    public @NonNull AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}

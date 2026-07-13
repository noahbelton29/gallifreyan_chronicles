package com.noahtnt2009.gallifreyan_chronicles.entity;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisExteriorBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.init.GCDataComponents;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.component.TardisComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

public class TardisKeyEntity extends Entity {
    public static Supplier<EntityType<TardisKeyEntity>> TYPE;

    private static final EntityDataAccessor<Float> KEY_WIDTH =
            SynchedEntityData.defineId(TardisKeyEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> KEY_HEIGHT =
            SynchedEntityData.defineId(TardisKeyEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> KEY_DEPTH =
            SynchedEntityData.defineId(TardisKeyEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<BlockPos> EXTERIOR_POS =
            SynchedEntityData.defineId(TardisKeyEntity.class, EntityDataSerializers.BLOCK_POS);

    public static final float DEFAULT_WIDTH = 0.1f;
    public static final float DEFAULT_HEIGHT = 0.1f;
    public static final float DEFAULT_DEPTH = DEFAULT_WIDTH;
    public static final Vec3 DEFAULT_OFFSET = new Vec3(0.0, -0.925, 1.0);

    public TardisKeyEntity(EntityType<? extends TardisKeyEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setInvisible(true);
    }

    public static TardisKeyEntity create(Level level, BlockPos exteriorPos, Vec3 worldPos) {
        return create(level, exteriorPos, worldPos, DEFAULT_OFFSET);
    }

    public static TardisKeyEntity create(Level level, BlockPos exteriorPos, Vec3 worldPos, Vec3 keyOffset) {
        TardisKeyEntity entity = new TardisKeyEntity(TYPE.get(), level);
        entity.setExteriorPos(exteriorPos);
        entity.setPos(worldPos.add(keyOffset));
        return entity;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(KEY_WIDTH, DEFAULT_WIDTH);
        builder.define(KEY_HEIGHT, DEFAULT_HEIGHT);
        builder.define(KEY_DEPTH, DEFAULT_DEPTH);
        builder.define(EXTERIOR_POS, BlockPos.ZERO);
    }

    @Override
    public boolean hurtServer(@NonNull ServerLevel serverLevel, @NonNull DamageSource damageSource, float v) {
        return false;
    }

    @Override
    public @NonNull EntityDimensions getDimensions(@NonNull Pose pose) {
        return EntityDimensions.scalable(this.getKeyWidth(), this.getKeyHeight());
    }

    private AABB computeKeyBoundingBox() {
        Vec3 pos = this.position();
        double halfWidth = this.getKeyWidth() / 2.0;
        double halfDepth = this.getKeyDepth() / 2.0;
        double halfHeight = this.getKeyHeight() / 2.0;
        return new AABB(
                pos.x - halfWidth, pos.y - halfHeight, pos.z - halfDepth,
                pos.x + halfWidth, pos.y + halfHeight, pos.z + halfDepth
        );
    }

    private void refreshKeyBoundingBox() {
        this.setBoundingBox(this.computeKeyBoundingBox());
    }

    @Override
    public void setPos(double x, double y, double z) {
        super.setPos(x, y, z);
        this.refreshKeyBoundingBox();
    }

    public float getKeyWidth() {
        return this.entityData.get(KEY_WIDTH);
    }
    public float getKeyHeight() {
        return this.entityData.get(KEY_HEIGHT);
    }
    public float getKeyDepth() {
        return this.entityData.get(KEY_DEPTH);
    }

    public void setKeySize(float width, float height, float depth) {
        this.entityData.set(KEY_WIDTH, width);
        this.entityData.set(KEY_HEIGHT, height);
        this.entityData.set(KEY_DEPTH, depth);
        this.refreshDimensions();
        this.refreshKeyBoundingBox();
    }

    public BlockPos getExteriorPos() {
        return this.entityData.get(EXTERIOR_POS);
    }

    public void setExteriorPos(BlockPos pos) {
        this.entityData.set(EXTERIOR_POS, pos);
    }

    public Vec3 getOffset() {
        return this.position().subtract(Vec3.atCenterOf(this.getExteriorPos()));
    }

    public void setOffset(Vec3 offset) {
        this.setPos(Vec3.atCenterOf(this.getExteriorPos()).add(offset));
    }

    @Nullable
    public TardisExteriorBlockEntity getExterior() {
        BlockPos pos = this.getExteriorPos();
        if (pos == null) return null;
        BlockEntity be = this.level().getBlockEntity(pos);
        if (be instanceof TardisExteriorBlockEntity exterior) return exterior;
        Constants.LOG.warn("Key entity at {} has no exterior block entity at {}", this.position(), pos);
        return null;
    }

    @Override
    public @NonNull InteractionResult interact(@NonNull Player player, @NonNull InteractionHand hand,
                                               @NonNull Vec3 location) {
        if (!player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }

        if (this.level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        TardisExteriorBlockEntity exterior = this.getExterior();
        if (exterior == null) {
            return InteractionResult.FAIL;
        }

        if (exterior.isLocked() && !exterior.hasKeyRendered()) {
            this.announceLocked(exterior, player);
            return InteractionResult.FAIL;
        }

        return exterior.hasKeyRendered()
                ? this.tryRemoveKey(exterior, player)
                : this.tryInsertKey(exterior, player, hand);
    }

    private void announceLocked(TardisExteriorBlockEntity exterior, Player player) {
        exterior.interact(false);

        Component overlay = Component.literal("Locked").withStyle(ChatFormatting.RED)
                .append(Component.literal(" - ").withStyle(ChatFormatting.GRAY))
                .append(this.ownerDisplayName(exterior));
        player.sendOverlayMessage(overlay);
    }

    private Component ownerDisplayName(TardisExteriorBlockEntity exterior) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return Component.literal("this TARDIS");
        }

        Optional<TardisComponent> tardis = exterior.resolveTardis(serverLevel);
        if (tardis.isEmpty()) {
            return Component.literal("this TARDIS");
        }

        UUID ownerId = tardis.get().getOwnerId();
        Player owner = serverLevel.getServer().getPlayerList().getPlayer(ownerId);
        String ownerName = owner != null ? owner.getGameProfile().name() : "an unknown Player";

        return Component.literal("this TARDIS belongs to ").append(Component.literal(ownerName)
                .withStyle(ChatFormatting.AQUA));
    }

    private InteractionResult tryInsertKey(TardisExteriorBlockEntity exterior, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);

        if (!(held.getItem() instanceof com.noahtnt2009.gallifreyan_chronicles.item.TardisKeyItem)) {
            return InteractionResult.FAIL;
        }

        UUID keyTardisId = held.get(GCDataComponents.TARDIS_ID);
        UUID exteriorTardisId = exterior.getTardisId();

        if (keyTardisId == null || !keyTardisId.equals(exteriorTardisId)) {
            return InteractionResult.FAIL;
        }

        boolean alreadyInserted = Boolean.TRUE.equals(held.get(GCDataComponents.KEY_INSERTED));
        if (alreadyInserted) {
            return InteractionResult.FAIL;
        }

        ItemStack toStore = held.copyWithCount(1);
        toStore.set(GCDataComponents.KEY_INSERTED, true);
        exterior.setHeldKey(player.getUUID(), toStore);
        held.shrink(1);

        return InteractionResult.CONSUME;
    }

    private InteractionResult tryRemoveKey(TardisExteriorBlockEntity exterior, Player player) {
        if (!player.getUUID().equals(exterior.getRenderedKeyOwner())) {
            return InteractionResult.FAIL;
        }

        ItemStack returned = exterior.clearHeldKey();
        returned.set(GCDataComponents.KEY_INSERTED, false);
        if (!player.getInventory().add(returned)) {
            player.drop(returned, false);
        }

        return InteractionResult.CONSUME;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        input.read("ExteriorPos", BlockPos.CODEC).ifPresent(this::setExteriorPos);

        float width = input.getFloatOr("KeyWidth", DEFAULT_WIDTH);
        float height = input.getFloatOr("KeyHeight", DEFAULT_HEIGHT);
        float depth = input.getFloatOr("KeyDepth", width);
        this.setKeySize(width, height, depth);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.store("ExteriorPos", BlockPos.CODEC, this.getExteriorPos());
        output.putFloat("KeyWidth", this.getKeyWidth());
        output.putFloat("KeyHeight", this.getKeyHeight());
        output.putFloat("KeyDepth", this.getKeyDepth());
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean skipAttackInteraction(@NonNull Entity attacker) {
        return true;
    }
}
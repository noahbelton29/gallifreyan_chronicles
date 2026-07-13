package com.noahtnt2009.gallifreyan_chronicles.entity;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisConsoleBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.TardisControl;
import com.noahtnt2009.gallifreyan_chronicles.tardis.control.TardisControlRegistry;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

public class TardisControlEntity extends Entity {
    public static Supplier<EntityType<TardisControlEntity>> TYPE;

    private static final EntityDataAccessor<Float> CONTROL_WIDTH =
            SynchedEntityData.defineId(TardisControlEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> CONTROL_HEIGHT =
            SynchedEntityData.defineId(TardisControlEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> CONTROL_DEPTH =
            SynchedEntityData.defineId(TardisControlEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<BlockPos> CONSOLE_POS =
            SynchedEntityData.defineId(TardisControlEntity.class, EntityDataSerializers.BLOCK_POS);
    private static final EntityDataAccessor<String> CONTROL_ID =
            SynchedEntityData.defineId(TardisControlEntity.class, EntityDataSerializers.STRING);

    public static final float DEFAULT_WIDTH = .1f;
    public static final float DEFAULT_HEIGHT = 0.25f;
    public static final float DEFAULT_DEPTH = DEFAULT_WIDTH;
    private static final long INTERACT_COOLDOWN_MS = 1500L;
    private long lastInteractTime = 0L;

    public TardisControlEntity(EntityType<? extends TardisControlEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setInvisible(true);
    }

    public static TardisControlEntity create(Level level, BlockPos consolePos, String controlId, Vec3 worldPos) {
        TardisControlEntity entity = new TardisControlEntity(TYPE.get(), level);
        entity.setConsolePos(consolePos);
        entity.setControlId(controlId);
        entity.setPos(worldPos);
        return entity;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(CONTROL_WIDTH, DEFAULT_WIDTH);
        builder.define(CONTROL_HEIGHT, DEFAULT_HEIGHT);
        builder.define(CONTROL_DEPTH, DEFAULT_DEPTH);
        builder.define(CONSOLE_POS, BlockPos.ZERO);
        builder.define(CONTROL_ID, "");
    }

    @Override
    public boolean hurtServer(@NonNull ServerLevel serverLevel, DamageSource damageSource, float v) {
        if (damageSource.getEntity() instanceof Player player) {
            TardisControl control = TardisControlRegistry.get(this.getControlId());
            return control.onLeftClick(this, player);
        }
        return false;
    }

    @Override
    public @NonNull EntityDimensions getDimensions(@NonNull Pose pose) {
        return EntityDimensions.scalable(this.getControlWidth(), this.getControlHeight());
    }

    private AABB computeControlBoundingBox() {
        Vec3 pos = this.position();
        double halfWidth = this.getControlWidth() / 2.0;
        double halfDepth = this.getControlDepth() / 2.0;
        double height = this.getControlHeight();
        return new AABB(
                pos.x - halfWidth, pos.y, pos.z - halfDepth,
                pos.x + halfWidth, pos.y + height, pos.z + halfDepth
        );
    }

    private void refreshControlBoundingBox() {
        this.setBoundingBox(this.computeControlBoundingBox());
    }

    @Override
    public void setPos(double x, double y, double z) {
        super.setPos(x, y, z);
        this.refreshControlBoundingBox();
    }

    public float getControlWidth() {
        return this.entityData.get(CONTROL_WIDTH);
    }

    public float getControlHeight() {
        return this.entityData.get(CONTROL_HEIGHT);
    }

    public float getControlDepth() {
        return this.entityData.get(CONTROL_DEPTH);
    }

    public void setControlSize(float width, float height, float depth) {
        this.entityData.set(CONTROL_WIDTH, width);
        this.entityData.set(CONTROL_HEIGHT, height);
        this.entityData.set(CONTROL_DEPTH, depth);
        this.refreshDimensions();
        this.refreshControlBoundingBox();
    }

    public BlockPos getConsolePos() {
        return this.entityData.get(CONSOLE_POS);
    }

    public void setConsolePos(BlockPos pos) {
        this.entityData.set(CONSOLE_POS, pos);
    }

    public String getControlId() {
        return this.entityData.get(CONTROL_ID);
    }

    public void setControlId(String id) {
        this.entityData.set(CONTROL_ID, id == null ? "" : id);
    }

    public Vec3 getOffset() {
        return this.position().subtract(Vec3.atCenterOf(this.getConsolePos()));
    }

    public void setOffset(Vec3 offset) {
        this.setPos(Vec3.atCenterOf(this.getConsolePos()).add(offset));
    }

    @Nullable
    public TardisConsoleBlockEntity getConsole() {
        if (this.getConsolePos() == null) return null;
        BlockEntity be = this.level().getBlockEntity(this.getConsolePos());
        if (be instanceof TardisConsoleBlockEntity console) return console;
        Constants.LOG.warn("Control entity '{}' at {} has no console block entity at {}",
                this.getControlId(), this.position(), this.getConsolePos());
        return null;
    }

    @Override
    public @NonNull InteractionResult interact(@NonNull Player player, @NonNull InteractionHand hand,
                                               @NonNull Vec3 location) {
        if (this.level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        long now = System.currentTimeMillis();
        if (now - this.lastInteractTime < INTERACT_COOLDOWN_MS) {
            return InteractionResult.FAIL;
        }
        this.lastInteractTime = now;

        TardisControl control = TardisControlRegistry.get(this.getControlId());
        InteractionResult result = control.onRightClick(this, player, hand);

        if (result == InteractionResult.SUCCESS) {
            TardisConsoleBlockEntity console = this.getConsole();
            if (console != null) {
                boolean currentState = console.getControlState(this.getControlId());
                boolean newState = !currentState;
                Constants.LOG.info("Control {} state toggled from {} to {}", this.getControlId(), currentState, newState);
                console.triggerControl(this.getControlId(), newState, player);
            }
        }

        return result;
    }

    public void printPosition(Player player) {
        Vec3 pos = this.position();
        Vec3 offset = this.getOffset();

        String idPart = this.getControlId().isEmpty() ? "<unset>" : this.getControlId();

        String chatLine = String.format(
                "[%s] pos=(%.4f, %.4f, %.4f) offset=(%.4f, %.4f, %.4f) size=(%.4f, %.4f, %.4f)",
                idPart, pos.x, pos.y, pos.z, offset.x, offset.y, offset.z,
                this.getControlWidth(), this.getControlHeight(), this.getControlDepth());

        player.sendSystemMessage(Component.literal(chatLine));
        Constants.LOG.info("TardisControlEntity {} -> {}", idPart, chatLine);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        input.read("ConsolePos", BlockPos.CODEC).ifPresent(this::setConsolePos);

        float width = input.getFloatOr("ControlWidth", DEFAULT_WIDTH);
        float height = input.getFloatOr("ControlHeight", DEFAULT_HEIGHT);
        float depth = input.getFloatOr("ControlDepth", width);
        this.setControlSize(width, height, depth);

        this.setControlId(input.getStringOr("ControlId", ""));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.store("ConsolePos", BlockPos.CODEC, this.getConsolePos());
        output.putFloat("ControlWidth", this.getControlWidth());
        output.putFloat("ControlHeight", this.getControlHeight());
        output.putFloat("ControlDepth", this.getControlDepth());
        output.putString("ControlId", this.getControlId());
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
package uk.nbforge.gallifreyan_chronicles.core.block;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import uk.nbforge.gallifreyan_chronicles.compat.portal.TardisPortalBridge;
import uk.nbforge.gallifreyan_chronicles.core.GCDataComponents;
import uk.nbforge.gallifreyan_chronicles.core.block_entity.TardisExteriorBlockEntity;
import uk.nbforge.gallifreyan_chronicles.core.data_registry.TardisExteriorRegistry;
import uk.nbforge.gallifreyan_chronicles.core.data_registry.TardisInteriorRegistry;
import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import uk.nbforge.gallifreyan_chronicles.tardis.TardisData;
import uk.nbforge.gallifreyan_chronicles.tardis.TardisDimensionUtil;
import uk.nbforge.gallifreyan_chronicles.tardis.TardisIdentity;
import uk.nbforge.gallifreyan_chronicles.tardis.TardisManager;
import uk.nbforge.gallifreyan_chronicles.tardis.deletion.TardisDeletion;
import uk.nbforge.gallifreyan_chronicles.tardis.exterior.ExteriorCollisionUtil;
import uk.nbforge.gallifreyan_chronicles.tardis.interior.TardisInterior;
import uk.nbforge.gallifreyan_chronicles.util.GCLog;
import uk.nbforge.gallifreyan_chronicles.util.MessageUtil;
import uk.nbforge.gallifreyan_chronicles.util.SchedulerUtil;

public class TardisBlock extends BaseEntityBlock {
    public static final int MAX_LIGHT = 12;
    public static final IntegerProperty LIGHT = IntegerProperty.create("light", 0, MAX_LIGHT);
    public static final BooleanProperty GLOWING = BooleanProperty.create("glowing");
    public static final BooleanProperty IN_FLIGHT = BooleanProperty.create("in_flight");
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 32, 16);

    public TardisBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(
                this.stateDefinition.any().setValue(GLOWING, false).setValue(LIGHT, 0));
        this.registerDefaultState(this.stateDefinition.any().setValue(IN_FLIGHT, false));
    }

    private static void applyChosenInterior(
            ServerLevel level, TardisExteriorBlockEntity blockEntity, TardisIdentity record, String chosen) {
        if (chosen == null || TardisInteriorRegistry.isUnknown(chosen)) return;

        TardisInterior interior = TardisInteriorRegistry.get(chosen);
        Tardis.get(level.getServer(), record.tardisId())
                .map(Tardis::data)
                .ifPresent(data -> data.setInteriorId(interior.id()));

        applyExteriorFromInterior(blockEntity, chosen);
    }

    /**
     * works on either side cuz it only changes the local block entity
     */
    private static void applyExteriorFromInterior(
            TardisExteriorBlockEntity blockEntity, @Nullable String chosenInterior) {
        if (chosenInterior == null || TardisInteriorRegistry.isUnknown(chosenInterior)) return;

        String exteriorId = TardisInteriorRegistry.get(chosenInterior).resolveExteriorId();
        if (TardisExteriorRegistry.contains(exteriorId)) {
            blockEntity.storeExterior(TardisExteriorRegistry.get(exteriorId));
        }
    }

    private static float snapYawTo45(float yaw) {
        float wrapped = yaw % 360.0f;
        if (wrapped < 0) wrapped += 360.0f;
        return Math.round(wrapped / 45.0f) * 45.0f % 360.0f;
    }

    public static void deleteIfCreative(Level level, BlockPos pos, Player player) {
        if (!(level instanceof ServerLevel serverLevel) || !player.isCreative()) return;
        if (!(level.getBlockEntity(pos) instanceof TardisExteriorBlockEntity exterior)) return;
        UUID tardisId = exterior.getTardisId();
        if (tardisId == null) return;
        TardisDeletion.request(tardisId, serverLevel, pos);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(GLOWING, LIGHT, IN_FLIGHT);
    }

    @Override
    public boolean canSurvive(@NonNull BlockState state, @NonNull LevelReader level, @NonNull BlockPos pos) {
        if (level instanceof Level realLevel && TardisDimensionUtil.isInterior(realLevel)) return false;
        return super.canSurvive(state, level, pos);
    }

    @Override
    public void setPlacedBy(
            @NonNull Level level,
            @NonNull BlockPos pos,
            @NonNull BlockState state,
            LivingEntity placer,
            @NonNull ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!(level.getBlockEntity(pos) instanceof TardisExteriorBlockEntity blockEntity)) return;
        String chosenInterior = stack.get(GCDataComponents.TARDIS_INTERIOR);
        stack.shrink(1);
        assert placer != null;
        blockEntity.setYaw(snapYawTo45(placer.getYRot() + 180));

        applyExteriorFromInterior(blockEntity, chosenInterior);

        if (!level.isClientSide()) {
            if (!(level instanceof ServerLevel serverLevel)) return;
            if (!(placer instanceof Player player)) return;

            TardisData data = TardisManager.get(serverLevel.getServer())
                    .createTardis(player.getUUID(), player.getGameProfile().name());
            data.setRegisteredBlockPos(pos);
            data.setExteriorDimension(serverLevel.dimension());
            TardisIdentity record = data.component();
            blockEntity.linkToTardis(record.tardisId());
            applyChosenInterior(serverLevel, blockEntity, record, chosenInterior);
            blockEntity.doors().snapShut();

            blockEntity.lock().spawnKeyEntity();

            Tardis.get(serverLevel.getServer(), record.tardisId()).ifPresent(tardis -> tardis.giveKeyTo(player));

            MinecraftServer server = serverLevel.getServer();
            UUID tardisId = record.tardisId();
            SchedulerUtil.runLater(
                    server, 1, () -> Tardis.get(server, tardisId).ifPresent(TardisPortalBridge::onExteriorPlaced));

            GCLog.CORE.debug(
                    "Registered TARDIS {} for player {}",
                    record.tardisId(),
                    player.getName().getString());
        }
    }

    @Override
    protected @NonNull ItemStack getCloneItemStack(
            @NonNull LevelReader level, @NonNull BlockPos pos, @NonNull BlockState state, boolean includeData) {
        ItemStack stack = super.getCloneItemStack(level, pos, state, includeData);
        if (level instanceof ServerLevel serverLevel
                && level.getBlockEntity(pos) instanceof TardisExteriorBlockEntity tardis) {
            UUID tardisId = tardis.getTardisId();
            if (tardisId != null) {
                TardisData data = Tardis.get(serverLevel.getServer(), tardisId)
                        .map(Tardis::data)
                        .orElse(null);
                String interiorId = data == null ? null : data.getInteriorId();
                if (interiorId != null) stack.set(GCDataComponents.TARDIS_INTERIOR, interiorId);
            }
        }
        return stack;
    }

    @Override
    protected @NonNull InteractionResult useWithoutItem(
            @NonNull BlockState state,
            Level level,
            @NonNull BlockPos pos,
            @NonNull Player player,
            @NonNull BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof TardisExteriorBlockEntity tardis)) {
            return InteractionResult.CONSUME;
        }

        if (tardis.travel().isFullyInvisible()) return InteractionResult.PASS;

        if (tardis.lock().hasKeyRendered()) {
            if (level.isClientSide()) return InteractionResult.SUCCESS;

            boolean isOwner = player.getUUID().equals(tardis.lock().getRenderedKeyOwner());

            if (player.isShiftKeyDown() && tardis.lock().canRemoveKey(player)) {
                if (tardis.doors().areFullyClosed()) {
                    Tardis.of(tardis).ifPresent(api -> api.returnKeyTo(player));
                    return InteractionResult.CONSUME;
                } else {
                    MessageUtil.overlay(player, "close_doors_to_remove_key");
                    return InteractionResult.FAIL;
                }
            } else if (!player.isShiftKeyDown() && isOwner) {
                tardis.lock().toggleKeyLockAnimation(player);
                return InteractionResult.CONSUME;
            }

            if (player.isShiftKeyDown()) {
                MessageUtil.overlay(player, "not_your_key");
            }

            return InteractionResult.CONSUME;
        }

        if (tardis.lock().isLocked()) {
            if (level.isClientSide()) return InteractionResult.SUCCESS;
            tardis.lock().announceLocked(player);
            return InteractionResult.CONSUME;
        }

        if (level.isClientSide()) return InteractionResult.SUCCESS;
        tardis.doors().interact(player.isShiftKeyDown());
        return InteractionResult.CONSUME;
    }

    @Override
    protected @NonNull VoxelShape getShape(
            @NonNull BlockState state,
            @NonNull BlockGetter level,
            @NonNull BlockPos pos,
            @NonNull CollisionContext context) {
        if (level.getBlockEntity(pos) instanceof TardisExteriorBlockEntity tardis
                && tardis.travel().isFullyInvisible()) {
            return Shapes.empty();
        }
        return SHAPE;
    }

    @Override
    protected void onPlace(
            @NonNull BlockState state,
            @NonNull Level level,
            @NonNull BlockPos pos,
            @NonNull BlockState oldState,
            boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide() && !oldState.is(this)) {
            TardisTopBlock.placeAbove(level, pos);
        }
    }

    @Override
    protected @NonNull VoxelShape getCollisionShape(
            @NonNull BlockState state,
            @NonNull BlockGetter level,
            @NonNull BlockPos pos,
            @NonNull CollisionContext context) {
        return ExteriorCollisionUtil.shape(level, pos);
    }

    @Override
    public @NonNull BlockState playerWillDestroy(
            @NonNull Level level, @NonNull BlockPos pos, @NonNull BlockState state, @NonNull Player player) {
        deleteIfCreative(level, pos, player);
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected float getShadeBrightness(@NonNull BlockState state, @NonNull BlockGetter level, @NonNull BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof TardisExteriorBlockEntity tardis
                && !tardis.travel().isShadowActive()) {
            return 1.0f;
        }
        return super.getShadeBrightness(state, level, pos);
    }

    @Override
    public @NonNull RenderShape getRenderShape(@NonNull BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public @NonNull BlockEntity newBlockEntity(@NonNull BlockPos pos, @NonNull BlockState state) {
        return new TardisExteriorBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            @NonNull Level level, @NonNull BlockState state, @NonNull BlockEntityType<T> type) {
        if (type != TardisExteriorBlockEntity.TYPE.get()) return null;
        if (level.isClientSide()) {
            return (lvl, _, _, be) -> TardisExteriorBlockEntity.clientTick(lvl, (TardisExteriorBlockEntity) be);
        }
        return (lvl, _, _, be) -> TardisExteriorBlockEntity.serverTick(lvl, (TardisExteriorBlockEntity) be);
    }
}

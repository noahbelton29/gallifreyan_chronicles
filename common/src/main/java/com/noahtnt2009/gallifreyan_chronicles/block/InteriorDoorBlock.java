package com.noahtnt2009.gallifreyan_chronicles.block;

import com.noahtnt2009.gallifreyan_chronicles.block.entity.InteriorDoorBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.init.GCBlocks;
import com.noahtnt2009.gallifreyan_chronicles.item.InteriorDoorBlockItem;
import com.noahtnt2009.gallifreyan_chronicles.init.GCDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import java.util.EnumMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class InteriorDoorBlock extends BaseEntityBlock {
    public static final EnumProperty<Direction> FACING =
            EnumProperty.create("door_facing", Direction.class, Direction.Plane.HORIZONTAL);
    public static final BooleanProperty FLIPPED = BooleanProperty.create("door_flipped");
    public static final BooleanProperty FLUSH = BooleanProperty.create("door_flush");
    public static final double WALL_ARRIVAL_SHIFT = 0.013;
    public static final int WIDTH = 2;
    public static final int HEIGHT = 3;
    public static final IntegerProperty PART_X = IntegerProperty.create("part_x", 0, WIDTH - 1);
    public static final IntegerProperty PART_Y = IntegerProperty.create("part_y", 0, HEIGHT - 1);

    private static final VoxelShape SHAPE_FLUSH = Block.box(0, 8, 0, 16, 16, 16);

    private static final int[] DEFAULT_DEPTH = {4, 12};
    private static final Map<String, int[]> MODEL_DEPTH = Map.of(
            "first_doctors_interior_doors", new int[]{12, 16}
    );
    private static final Map<Integer, EnumMap<Direction, VoxelShape>> SLABS = new java.util.concurrent.ConcurrentHashMap<>();

    private static VoxelShape slab(Direction facing, int[] depth) {
        EnumMap<Direction, VoxelShape> shapes = SLABS.computeIfAbsent(depth[0] * 100 + depth[1], k -> {
            int t0 = depth[0];
            int t1 = depth[1];
            EnumMap<Direction, VoxelShape> m = new EnumMap<>(Direction.class);
            m.put(Direction.NORTH, Block.box(0, 0, t0, 16, 16, t1));
            m.put(Direction.SOUTH, Block.box(0, 0, 16 - t1, 16, 16, 16 - t0));
            m.put(Direction.WEST, Block.box(t0, 0, 0, t1, 16, 16));
            m.put(Direction.EAST, Block.box(16 - t1, 0, 0, 16 - t0, 16, 16));
            return m;
        });
        return shapes.get(facing);
    }

    private int[] depthOf(BlockGetter level, BlockState state, BlockPos pos) {
        String model = this.variant;
        if (level.getBlockEntity(masterBlockPos(state, pos)) instanceof InteriorDoorBlockEntity door) {
            model = door.getVariant();
        }
        return MODEL_DEPTH.getOrDefault(model, DEFAULT_DEPTH);
    }

    private final String variant;
    private final boolean linked;
    private final String fixedVariantId;

    public InteriorDoorBlock(String variant, Properties properties) {
        this(variant, true, "", properties);
    }

    public InteriorDoorBlock(String variant, String fixedVariantId, Properties properties) {
        this(variant, false, fixedVariantId, properties);
    }

    private InteriorDoorBlock(String variant, boolean linked, String fixedVariantId, Properties properties) {
        super(properties);
        this.variant = variant;
        this.linked = linked;
        this.fixedVariantId = fixedVariantId;
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(FLIPPED, false)
                .setValue(FLUSH, false)
                .setValue(PART_X, 0)
                .setValue(PART_Y, 0));
    }

    public String variant() {
        return variant;
    }

    public boolean isLinked() {
        return linked;
    }

    public String fixedVariantId() {
        return fixedVariantId;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, FLIPPED, FLUSH, PART_X, PART_Y);
    }

    public static boolean isMaster(BlockState state) {
        return state.getValue(PART_X) == 0 && state.getValue(PART_Y) == 0;
    }

    public static Direction rightDirectionOf(Direction facing, boolean flipped) {
        return flipped ? facing.getClockWise() : facing.getCounterClockWise();
    }

    public static Direction rightDirectionOf(BlockState state) {
        return rightDirectionOf(state.getValue(FACING), isFlipped(state));
    }

    public static Direction upDirectionOf(Direction facing, boolean flipped, boolean flush) {
        if (flush) return facing;
        return flipped ? Direction.DOWN : Direction.UP;
    }

    public static Direction upDirectionOf(BlockState state) {
        return upDirectionOf(state.getValue(FACING), isFlipped(state), isFlush(state));
    }

    public static boolean isFlush(BlockState state) {
        return state.hasProperty(FLUSH) && state.getValue(FLUSH);
    }

    public static boolean isFlipped(BlockState state) {
        return state.hasProperty(FLIPPED) && state.getValue(FLIPPED);
    }

    public static BlockPos masterBlockPos(BlockState state, BlockPos pos) {
        return pos.relative(rightDirectionOf(state), -state.getValue(PART_X))
                .relative(upDirectionOf(state), -state.getValue(PART_Y));
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        boolean ceiling = context.getClickedFace() == Direction.DOWN;
        boolean flush = ceiling && context.isSecondaryUseActive();
        boolean flipped = ceiling && !flush;
        Direction right = rightDirectionOf(facing, flipped);
        Direction up = upDirectionOf(facing, flipped, flush);
        Level level = context.getLevel();
        BlockPos origin = context.getClickedPos();

        BlockState placed = this.defaultBlockState().setValue(FACING, facing).setValue(FLIPPED, flipped).setValue(FLUSH, flush);
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                BlockPos p = origin.relative(right, x).relative(up, y);
                if (level.isOutsideBuildHeight(p) || !level.getBlockState(p).canBeReplaced()) {
                    return null;
                }

                if (!level.isUnobstructed(placed, p, CollisionContext.empty())) {
                    return null;
                }
            }
        }
        return placed;
    }

    private static BlockPos barrierPos(BlockPos master, Direction facing, boolean flipped, int x, int y) {
        return master.relative(rightDirectionOf(facing, flipped), x).relative(upDirectionOf(facing, flipped, false), y)
                .relative(facing.getOpposite());
    }

    public static boolean isBackBlocked(Level level, BlockPos master, BlockState state) {
        if (isFlush(state)) return false;
        Direction facing = state.getValue(FACING);
        boolean flipped = isFlipped(state);
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                BlockPos p = barrierPos(master, facing, flipped, x, y);
                if (level.isOutsideBuildHeight(p)) continue;
                BlockState existing = level.getBlockState(p);
                if (existing.is(GCBlocks.INTERIOR_DOOR_BARRIER) || existing.canBeReplaced()) continue;
                if (existing.getCollisionShape(level, p).isEmpty()) continue;
                return true;
            }
        }
        return false;
    }

    public static void placeBarriers(Level level, BlockPos master, BlockState state) {

        removeBarriers(level, master, state);
    }

    public static void removeBarriers(Level level, BlockPos master, BlockState state) {
        if (level.isClientSide() || isFlush(state)) return;
        Direction facing = state.getValue(FACING);
        boolean flipped = isFlipped(state);
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                BlockPos p = barrierPos(master, facing, flipped, x, y);
                if (level.getBlockState(p).is(GCBlocks.INTERIOR_DOOR_BARRIER)) {
                    level.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS);
                }
            }
        }
    }

    @Override
    public void setPlacedBy(@NonNull Level level, @NonNull BlockPos pos, @NonNull BlockState state,
                            @Nullable LivingEntity placer, @NonNull ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.isClientSide()) return;
        if (linked && stack.get(GCDataComponents.DOOR_TYPE) != null
                && level.getBlockEntity(pos) instanceof InteriorDoorBlockEntity door) {
            door.setDoorVariant(InteriorDoorBlockItem.selectedVariant(stack));
        }

        Direction right = rightDirectionOf(state);
        Direction up = upDirectionOf(state);
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                if (x == 0 && y == 0) continue;
                level.setBlock(pos.relative(right, x).relative(up, y),
                        state.setValue(PART_X, x).setValue(PART_Y, y), Block.UPDATE_ALL);
            }
        }
        placeBarriers(level, pos, state);
    }

    @Override
    public @NotNull BlockState playerWillDestroy(@NotNull Level level, @NotNull BlockPos pos,
                                                 @NotNull BlockState state, @NotNull Player player) {
        if (!level.isClientSide()) {
            BlockPos master = masterBlockPos(state, pos);
            Direction right = rightDirectionOf(state);
            Direction up = upDirectionOf(state);
            for (int x = 0; x < WIDTH; x++) {
                for (int y = 0; y < HEIGHT; y++) {
                    BlockPos p = master.relative(right, x).relative(up, y);
                    if (p.equals(pos)) continue;
                    if (level.getBlockState(p).is(this)) {
                        level.setBlock(p, Blocks.AIR.defaultBlockState(),
                                Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS);
                    }
                }
            }
            removeBarriers(level, master, state);
            if (!player.isCreative()) {
                Block.popResource(level, master, new ItemStack(this));
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected @NonNull InteractionResult useWithoutItem(@NonNull BlockState state, Level level, @NonNull BlockPos pos,
                                                        @NonNull Player player, @NonNull BlockHitResult hitResult) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (level.getBlockEntity(masterBlockPos(state, pos)) instanceof InteriorDoorBlockEntity door) {
            door.interact(player.isShiftKeyDown());
        }
        return InteractionResult.CONSUME;
    }

    @Override
    protected @NonNull VoxelShape getShape(@NonNull BlockState state, @NonNull BlockGetter level,
                                           @NonNull BlockPos pos, @NonNull CollisionContext context) {
        // Seen from outside through the exterior's portal: no selection shape, so no outline (and the
        // targeting ray passes through the door instead of selecting it).
        if (level instanceof Level l && l.isClientSide()
                && com.noahtnt2009.gallifreyan_chronicles.client.renderer.RemoteDoorView.isSeenThroughPortal(l)) {
            return Shapes.empty();
        }
        if (isFlush(state)) return SHAPE_FLUSH;
        return slab(state.getValue(FACING), depthOf(level, state, pos));
    }

    @Override
    protected @NonNull VoxelShape getCollisionShape(@NonNull BlockState state, @NonNull BlockGetter level,
                                                    @NonNull BlockPos pos, @NonNull CollisionContext context) {
        if (level.getBlockEntity(masterBlockPos(state, pos)) instanceof InteriorDoorBlockEntity door
                && !door.getDoorState().isClosed()) {
            return Shapes.empty();
        }
        return getShape(state, level, pos, context);
    }

    @Override
    protected @NonNull BlockState rotate(@NonNull BlockState state, @NonNull Rotation rotation) {
        return state;
    }

    @Override
    protected @NonNull BlockState mirror(@NonNull BlockState state, @NonNull Mirror mirror) {
        return state;
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return isMaster(state) ? new InteriorDoorBlockEntity(pos, state) : null;
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            @NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return type == InteriorDoorBlockEntity.TYPE.get()
                ? (_, _, _, be) -> ((InteriorDoorBlockEntity) be).serverTick()
                : null;
    }
}
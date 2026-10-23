package com.noahtnt2009.gallifreyan_chronicles.block;

import com.noahtnt2009.gallifreyan_chronicles.block.entity.InteriorDoorBlockEntity;
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
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class InteriorDoorBlock extends BaseEntityBlock {
    public static final EnumProperty<Direction> FACING =
            EnumProperty.create("door_facing", Direction.class, Direction.Plane.HORIZONTAL);
    public static final int WIDTH = 2;
    public static final int HEIGHT = 3;
    public static final IntegerProperty PART_X = IntegerProperty.create("part_x", 0, WIDTH - 1);
    public static final IntegerProperty PART_Y = IntegerProperty.create("part_y", 0, HEIGHT - 1);

    private static final VoxelShape SHAPE_Z = Block.box(0, 0, 4, 16, 16, 12);
    private static final VoxelShape SHAPE_X = Block.box(4, 0, 0, 12, 16, 16);

    private final String variant;
    private final boolean linked;

    public InteriorDoorBlock(String variant, boolean linked, Properties properties) {
        super(properties);
        this.variant = variant;
        this.linked = linked;
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(PART_X, 0)
                .setValue(PART_Y, 0));
    }

    public String variant() {
        return variant;
    }

    public boolean isLinked() {
        return linked;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART_X, PART_Y);
    }

    public static boolean isMaster(BlockState state) {
        return state.getValue(PART_X) == 0 && state.getValue(PART_Y) == 0;
    }

    public static Direction rightOf(Direction facing) {
        return facing.getCounterClockWise();
    }

    public static BlockPos masterPos(BlockState state, BlockPos pos) {
        return pos.relative(rightOf(state.getValue(FACING)), -state.getValue(PART_X))
                .below(state.getValue(PART_Y));
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        Direction right = rightOf(facing);
        Level level = context.getLevel();
        BlockPos origin = context.getClickedPos();

        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                BlockPos p = origin.relative(right, x).above(y);
                if (level.isOutsideBuildHeight(p) || !level.getBlockState(p).canBeReplaced()) {
                    return null;
                }
            }
        }
        return this.defaultBlockState().setValue(FACING, facing);
    }

    @Override
    public void setPlacedBy(@NonNull Level level, @NonNull BlockPos pos, @NonNull BlockState state,
                            @Nullable LivingEntity placer, @NonNull ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.isClientSide()) return;

        Direction right = rightOf(state.getValue(FACING));
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                if (x == 0 && y == 0) continue;
                level.setBlock(pos.relative(right, x).above(y),
                        state.setValue(PART_X, x).setValue(PART_Y, y), Block.UPDATE_ALL);
            }
        }
    }

    @Override
    public @NotNull BlockState playerWillDestroy(@NotNull Level level, @NotNull BlockPos pos,
                                                 @NotNull BlockState state, @NotNull Player player) {
        if (!level.isClientSide()) {
            BlockPos master = masterPos(state, pos);
            Direction right = rightOf(state.getValue(FACING));
            for (int x = 0; x < WIDTH; x++) {
                for (int y = 0; y < HEIGHT; y++) {
                    BlockPos p = master.relative(right, x).above(y);
                    if (p.equals(pos)) continue;
                    if (level.getBlockState(p).is(this)) {
                        level.setBlock(p, Blocks.AIR.defaultBlockState(),
                                Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS);
                    }
                }
            }
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
        if (level.getBlockEntity(masterPos(state, pos)) instanceof InteriorDoorBlockEntity door) {
            door.interact(player.isShiftKeyDown());
        }
        return InteractionResult.CONSUME;
    }

    @Override
    protected @NonNull VoxelShape getShape(@NonNull BlockState state, @NonNull BlockGetter level,
                                           @NonNull BlockPos pos, @NonNull CollisionContext context) {
        return state.getValue(FACING).getAxis() == Direction.Axis.Z ? SHAPE_Z : SHAPE_X;
    }

    @Override
    protected @NonNull VoxelShape getCollisionShape(@NonNull BlockState state, @NonNull BlockGetter level,
                                                    @NonNull BlockPos pos, @NonNull CollisionContext context) {
        if (level.getBlockEntity(masterPos(state, pos)) instanceof InteriorDoorBlockEntity door
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

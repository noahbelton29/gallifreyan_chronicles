package uk.nbforge.gallifreyan_chronicles.core.block;

import uk.nbforge.gallifreyan_chronicles.util.PlayerMotionUtil;

import uk.nbforge.gallifreyan_chronicles.util.ConsoleStackUtil;

import uk.nbforge.gallifreyan_chronicles.core.block_entities.TardisConsoleBlockEntity;
import uk.nbforge.gallifreyan_chronicles.core.GCDataComponents;
import uk.nbforge.gallifreyan_chronicles.core.GCSounds;
import uk.nbforge.gallifreyan_chronicles.tardis.console.TardisConsole;
import uk.nbforge.gallifreyan_chronicles.core.dynamic_registries.TardisConsoleRegistry;
import uk.nbforge.gallifreyan_chronicles.core.block_entities.TardisExteriorBlockEntity;
import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import uk.nbforge.gallifreyan_chronicles.tardis.TardisApi;
import uk.nbforge.gallifreyan_chronicles.tardis.ecs.system.ConsoleLinkSystem;
import uk.nbforge.gallifreyan_chronicles.tardis.ecs.system.ConsoleSyncSystem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class ConsoleBlock extends BaseEntityBlock {

    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<Half> HALF = BlockStateProperties.HALF;

    public ConsoleBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(
                this.stateDefinition.any()
                        .setValue(FACING, Direction.NORTH)
                        .setValue(HALF, Half.BOTTOM)
        );
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, double fallDistance) {
        repel(level, pos, entity);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState onState, Entity entity) {
        repel(level, pos, entity);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, HALF);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        Half half = context.getClickedFace() == Direction.DOWN ? Half.TOP : Half.BOTTOM;

        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(HALF, half);
    }

    public static void repel(Level level, BlockPos pos, Entity entity) {
        if (!(level instanceof ServerLevel)) return;
        /* only a console inside a tardis throws you off */
        if (level.getBlockEntity(pos) instanceof TardisConsoleBlockEntity console && !console.isInTardis()) return;
        if (!(entity instanceof LivingEntity livingEntity)) return;

        float yaw = livingEntity.getYRot() * ((float) Math.PI / 180F);
        double power = 0.3;

        PlayerMotionUtil.set(livingEntity, new Vec3(Math.sin(yaw) * power, 0.3, -Math.cos(yaw) * power));
        level.playSound(null, pos, GCSounds.TARDIS_CLOISTER_BELL, SoundSource.BLOCKS);
    }

    public static boolean isUpsideDown(BlockState state) {
        return state.hasProperty(HALF) && state.getValue(HALF) == Half.TOP;
    }

    @Override
    protected @NonNull BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected @NonNull BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public @NotNull BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new TardisConsoleBlockEntity(pos, state);
    }

    @Override
    public void setPlacedBy(@NonNull Level level, @NonNull BlockPos pos, @NonNull BlockState state,
                            @Nullable LivingEntity placer, @NonNull ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        if (level.isClientSide()) return;
        if (!(level.getBlockEntity(pos) instanceof TardisConsoleBlockEntity console)) return;

        String consoleId = stack.get(GCDataComponents.CONSOLE_ID);
        TardisConsole selected = (consoleId != null && TardisConsoleRegistry.contains(consoleId))
                ? TardisConsoleRegistry.get(consoleId)
                : TardisConsoleRegistry.get(TardisConsoleRegistry.defaultId());

        String variantId = stack.get(GCDataComponents.CONSOLE_VARIANT);
        if (variantId != null && selected.hasVariant(variantId)) {
            console.setConsole(selected, variantId);
        } else {
            console.setConsole(selected);
        }

        ConsoleLinkSystem.onPlaced(console);
    }

    @Override
    protected void affectNeighborsAfterRemoval(@NonNull BlockState state, @NonNull ServerLevel level,
                                               @NonNull BlockPos pos, boolean movedByPiston) {
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);

        Tardis tardis = TardisApi.ofLevel(level).orElse(null);
        if (tardis == null) return;

        for (TardisConsoleBlockEntity other : ConsoleSyncSystem.consolesOf(tardis.id())) {
            if (!other.getBlockPos().equals(pos)) return;
        }
        tardis.exteriorIfLoaded().ifPresent(TardisExteriorBlockEntity::cutPower);
    }

    @Override
    protected @NonNull ItemStack getCloneItemStack(@NonNull LevelReader level, @NonNull BlockPos pos,
                                                   @NonNull BlockState state, boolean includeData) {
        ItemStack stack = super.getCloneItemStack(level, pos, state, includeData);
        if (!(level.getBlockEntity(pos) instanceof TardisConsoleBlockEntity console)) return stack;
        return ConsoleStackUtil.withConsoleData(stack, console);
    }
}
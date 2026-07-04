package com.noahtnt2009.gallifreyan_chronicles.block;

import com.mojang.serialization.MapCodec;
import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisExteriorBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.ecs.Entity;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.system.TardisLinkSystem;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.component.TardisComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class TardisBlock extends BaseEntityBlock {
    public static final BooleanProperty GLOWING = BooleanProperty.create("glowing");

    public TardisBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(GLOWING, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(GLOWING);
    }

    @Override
    public void setPlacedBy(@NonNull Level level, @NonNull BlockPos pos, @NonNull BlockState state, LivingEntity placer,
                            @NonNull ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!(level.getBlockEntity(pos) instanceof TardisExteriorBlockEntity blockEntity)) return;

        blockEntity.setYaw(placer.getYRot() + 180);

        if (!level.isClientSide()) {
            if (!(level instanceof ServerLevel serverLevel)) return;
            if (!(placer instanceof Player player)) return;

            Entity entity = blockEntity.asEntity();
            TardisComponent record = TardisLinkSystem.registerNewTardis(entity, serverLevel, player.getUUID(), pos);
            blockEntity.sync();

            Constants.LOG.debug(
                    "Registered TARDIS {} for player {}",
                    record.getTardisId(),
                    player.getName().getString()
            );
        }
    }

    @Override
    protected @NonNull InteractionResult useWithoutItem(@NonNull BlockState state, Level level, @NonNull BlockPos pos,
                                                        @NonNull Player player, @NonNull BlockHitResult hitResult) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (level.getBlockEntity(pos) instanceof TardisExteriorBlockEntity tardis) {
            tardis.interact(player.isShiftKeyDown());
        }
        return InteractionResult.CONSUME;
    }

    @Override
    protected @NonNull MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(TardisBlock::new);
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public @NotNull BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new TardisExteriorBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            @NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return type == TardisExteriorBlockEntity.TYPE.get()
                ? (lvl, pos, st, be) -> TardisExteriorBlockEntity.serverTick(lvl, pos, st, (TardisExteriorBlockEntity) be)
                : null;
    }
}
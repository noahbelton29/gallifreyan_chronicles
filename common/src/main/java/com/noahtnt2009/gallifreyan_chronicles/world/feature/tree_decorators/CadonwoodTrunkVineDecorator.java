package com.noahtnt2009.gallifreyan_chronicles.world.feature.tree_decorators;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.noahtnt2009.gallifreyan_chronicles.init.GCBlocks;
import com.noahtnt2009.gallifreyan_chronicles.init.GCTreeDecoratorTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import org.jspecify.annotations.NonNull;

public class CadonwoodTrunkVineDecorator extends TreeDecorator {
    public static final MapCodec<CadonwoodTrunkVineDecorator> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    BlockState.CODEC.fieldOf("vine_state").forGetter(d -> d.vineState),
                    Codec.floatRange(0.0F, 1.0F).fieldOf("probability").forGetter(d -> d.probability)
            ).apply(instance, CadonwoodTrunkVineDecorator::new)
    );

    private final BlockState vineState;
    private final float probability;

    public CadonwoodTrunkVineDecorator(BlockState vineState, float probability) {
        this.vineState = vineState;
        this.probability = probability;
    }

    @Override
    protected @NonNull TreeDecoratorType<?> type() {
        return GCTreeDecoratorTypes.CADONWOOD_TRUNK_VINE;
    }

    public void place(Context context) {
        RandomSource random = context.random();
        context.logs().forEach((pos) -> {
            if (random.nextInt(3) > 0) {
                BlockPos west = pos.west();
                if (context.isAir(west)) {
                    context.setBlock(west, GCBlocks.GALLIFREYAN_VINE.defaultBlockState().setValue(VineBlock.EAST, true));
                }
            }
            if (random.nextInt(3) > 0) {
                BlockPos east = pos.east();
                if (context.isAir(east)) {
                    context.setBlock(east, GCBlocks.GALLIFREYAN_VINE.defaultBlockState().setValue(VineBlock.WEST, true));
                }
            }
            if (random.nextInt(3) > 0) {
                BlockPos north = pos.north();
                if (context.isAir(north)) {
                    context.setBlock(north, GCBlocks.GALLIFREYAN_VINE.defaultBlockState().setValue(VineBlock.SOUTH, true));
                }
            }
            if (random.nextInt(3) > 0) {
                BlockPos south = pos.south();
                if (context.isAir(south)) {
                    context.setBlock(south, GCBlocks.GALLIFREYAN_VINE.defaultBlockState().setValue(VineBlock.NORTH, true));
                }
            }
        });
    }

    private net.minecraft.world.level.block.state.properties.BooleanProperty getDirectionProperty(Direction direction) {
        return switch (direction) {
            case NORTH -> VineBlock.NORTH;
            case SOUTH -> VineBlock.SOUTH;
            case EAST -> VineBlock.EAST;
            case WEST -> VineBlock.WEST;
            default -> throw new IllegalArgumentException("Invalid direction for vine: " + direction);
        };
    }
}

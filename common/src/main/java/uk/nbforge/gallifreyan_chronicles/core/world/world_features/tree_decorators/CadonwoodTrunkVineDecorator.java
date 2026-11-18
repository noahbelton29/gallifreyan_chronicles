package uk.nbforge.gallifreyan_chronicles.core.world.world_features.tree_decorators;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import org.jspecify.annotations.NonNull;
import uk.nbforge.gallifreyan_chronicles.core.GCTreeDecoratorTypes;
import uk.nbforge.gallifreyan_chronicles.core.block.registry.FloraBlocks;

public class CadonwoodTrunkVineDecorator extends TreeDecorator {
    public static final MapCodec<CadonwoodTrunkVineDecorator> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                            BlockState.CODEC.fieldOf("vine_state").forGetter(CadonwoodTrunkVineDecorator::vineState),
                            Codec.floatRange(0.0f, 1.0f)
                                    .fieldOf("probability")
                                    .forGetter(CadonwoodTrunkVineDecorator::probability))
                    .apply(instance, CadonwoodTrunkVineDecorator::new));

    private final BlockState vineState;
    private final float probability;

    public BlockState vineState() {
        return vineState;
    }

    public float probability() {
        return probability;
    }

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
                    context.setBlock(
                            west,
                            FloraBlocks.GALLIFREYAN_VINE.defaultBlockState().setValue(VineBlock.EAST, true));
                }
            }
            if (random.nextInt(3) > 0) {
                BlockPos east = pos.east();
                if (context.isAir(east)) {
                    context.setBlock(
                            east,
                            FloraBlocks.GALLIFREYAN_VINE.defaultBlockState().setValue(VineBlock.WEST, true));
                }
            }
            if (random.nextInt(3) > 0) {
                BlockPos north = pos.north();
                if (context.isAir(north)) {
                    context.setBlock(
                            north,
                            FloraBlocks.GALLIFREYAN_VINE.defaultBlockState().setValue(VineBlock.SOUTH, true));
                }
            }
            if (random.nextInt(3) > 0) {
                BlockPos south = pos.south();
                if (context.isAir(south)) {
                    context.setBlock(
                            south,
                            FloraBlocks.GALLIFREYAN_VINE.defaultBlockState().setValue(VineBlock.NORTH, true));
                }
            }
        });
    }

    private BooleanProperty getDirectionProperty(Direction direction) {
        return switch (direction) {
            case NORTH -> VineBlock.NORTH;
            case SOUTH -> VineBlock.SOUTH;
            case EAST -> VineBlock.EAST;
            case WEST -> VineBlock.WEST;
            default -> throw new IllegalArgumentException("Invalid direction for vine: " + direction);
        };
    }
}

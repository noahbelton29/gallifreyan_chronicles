package uk.nbforge.gallifreyan_chronicles.codec.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.state.BlockState;
import uk.nbforge.gallifreyan_chronicles.core.world.world_features.GCBoulderFeature;
import uk.nbforge.gallifreyan_chronicles.core.world.world_features.tree_decorators.CadonwoodTrunkVineDecorator;

public final class WorldgenCodec {
    public static final MapCodec<CadonwoodTrunkVineDecorator> CADONWOOD_TRUNK_VINE_DECORATOR = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    BlockState.CODEC.fieldOf("vine_state").forGetter(CadonwoodTrunkVineDecorator::vineState),
                    Codec.floatRange(0.0F, 1.0F).fieldOf("probability").forGetter(CadonwoodTrunkVineDecorator::probability)
            ).apply(instance, CadonwoodTrunkVineDecorator::new)
    );

    public static final MapCodec<GCBoulderFeature> GC_BOULDER_FEATURE = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BuiltInRegistries.BLOCK.byNameCodec()
                    .listOf()
                    .fieldOf("blocks")
                    .forGetter(GCBoulderFeature::blocks)
    ).apply(instance, GCBoulderFeature::new));

    private WorldgenCodec() {
    }
}

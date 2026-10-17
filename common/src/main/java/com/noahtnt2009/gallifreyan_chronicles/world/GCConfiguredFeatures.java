package com.noahtnt2009.gallifreyan_chronicles.world;

import com.noahtnt2009.gallifreyan_chronicles.init.GCBlocks;
import com.noahtnt2009.gallifreyan_chronicles.init.GCTags;
import com.noahtnt2009.gallifreyan_chronicles.util.GCUtils;
import com.noahtnt2009.gallifreyan_chronicles.world.feature.GCBoulderFeature;
import com.noahtnt2009.gallifreyan_chronicles.world.feature.tree_decorators.CadonwoodTrunkVineDecorator;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.random.WeightedList;
import net.minecraft.util.valueproviders.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.BlockColumnFeature;
import net.minecraft.world.level.levelgen.feature.FallenTreeFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.OreFeature;
import net.minecraft.world.level.levelgen.feature.ReplaceBlobsFeature;
import net.minecraft.world.level.levelgen.feature.SimpleBlockFeature;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.BlobFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.AttachedToLogsDecorator;
import net.minecraft.world.level.levelgen.feature.trunkplacers.StraightTrunkPlacer;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

import java.util.List;

public class GCConfiguredFeatures {
    public static final ResourceKey<Feature> IRON_GALLIFREY_ORES_KEY = registerKey("iron_gallifrey_ores");
    public static final ResourceKey<Feature> IRON_GALLIFREY_ORES_SMALL_KEY = registerKey("iron_gallifrey_ores_small");
    public static final ResourceKey<Feature> COAL_GALLIFREY_ORES_KEY = registerKey("coal_gallifrey_ores");
    public static final ResourceKey<Feature> GRAVEL_GALLIFREY_ORES_KEY = registerKey("gravel_gallifrey_ores");
    public static final ResourceKey<Feature> COBBLED_LIMESTONE_GALLIFREY_ORES_KEY = registerKey("cobbled_limestone_gallifrey_ores");
    public static final ResourceKey<Feature> GALLIFREYAN_DRY_GRASS = registerKey("gallifreyan_dry_grass");
    public static final ResourceKey<Feature> GALLIFREYAN_GRASS = registerKey("gallifreyan_grass");
    public static final ResourceKey<Feature> GALLIFREYAN_TALL_GRASS = registerKey("gallifreyan_tall_grass");
    public static final ResourceKey<Feature> CADONWOOD_TREE = registerKey("cadonwood_tree");
    public static final ResourceKey<Feature> FALLEN_CADONWOOD_TREE = registerKey("fallen_cadonwood_tree");
    public static final ResourceKey<Feature> GALLIFREYAN_STONE_BOULDER = registerKey("gallifreyan_stone_boulder");
    public static final ResourceKey<Feature> GALLIFREYAN_ARCADIAN_SHALE_BOULDER = registerKey("gallifreyan_arcadian_shale_boulder");
    public static final ResourceKey<Feature> MOON_BOULDER = registerKey("moon_boulder");
    public static final ResourceKey<Feature> GALLIFREYAN_STONE_PATCH = registerKey("gallifreyan_stone_patch");
    public static final ResourceKey<Feature> GALLIFREYAN_MUDSTONE_PATCH = registerKey("gallifreyan_mudstone_patch");
    public static final ResourceKey<Feature> THORIVINE = registerKey("thorivine");

    public static void bootstrap(BootstrapContext<Feature> context) {
        var stoneReplaceables = new TagMatchTest(GCTags.Blocks.GALLIFREYAN_STONE_REPLACEABLES);
        var baseStone = new TagMatchTest(GCTags.Blocks.BASE_STONE_GALLIFREY);

        register(context, CADONWOOD_TREE, new TreeFeature.Builder(
                BlockStateProvider.of(GCBlocks.CADONWOOD_LOG),
                new StraightTrunkPlacer(4, 2, 0),
                BlockStateProvider.of(GCBlocks.CADONWOOD_LEAVES),
                new BlobFoliagePlacer(ConstantInt.of(2), ConstantInt.of(0), 3),
                new TwoLayersFeatureSize(1, 0, 1),
                BlockStateProvider.holderOf(GCBlocks.GALLIFREYAN_DIRT))
                .build());

        register(context, IRON_GALLIFREY_ORES_KEY, new OreFeature(stoneReplaceables, GCBlocks.GALLIFREYAN_STONE_IRON_ORE.defaultBlockState(), 9));
        register(context, IRON_GALLIFREY_ORES_SMALL_KEY, new OreFeature(stoneReplaceables, GCBlocks.GALLIFREYAN_STONE_IRON_ORE.defaultBlockState(), 4));
        register(context, COAL_GALLIFREY_ORES_KEY, new OreFeature(stoneReplaceables, GCBlocks.GALLIFREYAN_STONE_COAL_ORE.defaultBlockState(), 17));
        register(context, GRAVEL_GALLIFREY_ORES_KEY, new OreFeature(baseStone, GCBlocks.GALLIFREYAN_GRAVEL.defaultBlockState(), 33));
        register(context, COBBLED_LIMESTONE_GALLIFREY_ORES_KEY, new OreFeature(baseStone, GCBlocks.COBBLED_LIMESTONE.defaultBlockState(), 26));

        register(context, GALLIFREYAN_DRY_GRASS, new SimpleBlockFeature(new WeightedStateProvider(WeightedList.<BlockState>builder()
                .add(GCBlocks.SHORT_GALLIFREYAN_DRY_GRASS.defaultBlockState(), 1)
                .add(GCBlocks.TALL_GALLIFREYAN_DRY_GRASS.defaultBlockState(), 1)
                .build())));
        register(context, GALLIFREYAN_GRASS, new SimpleBlockFeature(BlockStateProvider.of(GCBlocks.SHORT_GALLIFREYAN_GRASS)));
        register(context, GALLIFREYAN_TALL_GRASS, new SimpleBlockFeature(BlockStateProvider.of(GCBlocks.TALL_GALLIFREYAN_GRASS)));

        register(context, FALLEN_CADONWOOD_TREE, createFallenCadonwood().build());

        register(context, GALLIFREYAN_STONE_BOULDER, new GCBoulderFeature(List.of(
                GCBlocks.GALLIFREYAN_STONE,
                GCBlocks.GALLIFREYAN_COBBLESTONE
        )));
        register(context, MOON_BOULDER, new GCBoulderFeature(List.of(
                GCBlocks.LUNAR_REGOLITH,
                GCBlocks.MOONSLATE
        )));
        register(context, GALLIFREYAN_ARCADIAN_SHALE_BOULDER, new GCBoulderFeature(List.of(
                GCBlocks.ARCADIAN_SHALE,
                GCBlocks.GALLIFREYAN_MUD,
                GCBlocks.GALLIFREYAN_MUDSTONE
        )));

        register(context, GALLIFREYAN_STONE_PATCH, new ReplaceBlobsFeature(
                GCBlocks.GALLIFREYAN_SAND.defaultBlockState(),
                GCBlocks.GALLIFREYAN_STONE.defaultBlockState(),
                UniformInt.of(3, 5)
        ));

        register(context, GALLIFREYAN_MUDSTONE_PATCH, new ReplaceBlobsFeature(
                GCBlocks.ARCADIAN_SHALE.defaultBlockState(),
                GCBlocks.GALLIFREYAN_MUDSTONE.defaultBlockState(),
                UniformInt.of(5, 8)
        ));

        register(context, THORIVINE, new BlockColumnFeature(
                List.of(
                        BlockColumnFeature.layer(
                                BiasedToBottomInt.of(2, 5),
                                BlockStateProvider.of(GCBlocks.THORIVINE)
                        ),
                        BlockColumnFeature.layer(
                                new WeightedListInt(WeightedList.<IntProvider>builder()
                                        .add(ConstantInt.of(0), 5)
                                        .add(ConstantInt.of(1), 1)
                                        .build()),
                                BlockStateProvider.of(GCBlocks.THORIVINE_FLOWER)
                        )
                ),
                Direction.UP,
                BlockPredicate.ONLY_IN_AIR_PREDICATE,
                false
        ));
    }

    private static FallenTreeFeature.Builder createFallenTrees() {
        return FallenTreeFeature.builder(
                BlockStateProvider.of(GCBlocks.CADONWOOD_LOG),
                UniformInt.of(4, 7)
        ).logDecorator(
                new AttachedToLogsDecorator(
                        0.1F,
                        Holder.direct(new WeightedStateProvider(
                                WeightedList.<BlockState>builder()
                                        .add(Blocks.RED_MUSHROOM.defaultBlockState(), 2)
                                        .add(Blocks.BROWN_MUSHROOM.defaultBlockState(), 1)
                                        .build()
                        )),
                        List.of(Direction.UP)
                )
        );
    }

    private static FallenTreeFeature.Builder createFallenCadonwood() {
        return createFallenTrees()
                .stumpDecorator(
                        new CadonwoodTrunkVineDecorator(
                                GCBlocks.GALLIFREYAN_VINE.defaultBlockState(),
                                0.5f
                        )
                );
    }

    public static ResourceKey<Feature> registerKey(String name) {
        return ResourceKey.create(Registries.FEATURE, GCUtils.of(name));
    }

    private static void register(BootstrapContext<Feature> context, ResourceKey<Feature> key, Feature feature) {
        context.register(key, feature);
    }
}
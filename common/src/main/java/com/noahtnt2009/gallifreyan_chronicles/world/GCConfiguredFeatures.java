package com.noahtnt2009.gallifreyan_chronicles.world;

import com.google.common.collect.ImmutableList;
import com.noahtnt2009.gallifreyan_chronicles.init.GCBlocks;
import com.noahtnt2009.gallifreyan_chronicles.init.GCFeatures;
import com.noahtnt2009.gallifreyan_chronicles.init.GCTags;
import com.noahtnt2009.gallifreyan_chronicles.util.GCUtils;
import com.noahtnt2009.gallifreyan_chronicles.world.feature.config.BoulderFeatureConfig;
import com.noahtnt2009.gallifreyan_chronicles.world.feature.tree_decorators.CadonwoodTrunkVineDecorator;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.random.WeightedList;
import net.minecraft.util.valueproviders.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.*;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.BlobFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.AttachedToLogsDecorator;
import net.minecraft.world.level.levelgen.feature.trunkplacers.StraightTrunkPlacer;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

import java.util.List;

public class GCConfiguredFeatures {
    public static final ResourceKey<ConfiguredFeature<?, ?>> IRON_GALLIFREY_ORES_KEY = registerKey("iron_gallifrey_ores");
    public static final ResourceKey<ConfiguredFeature<?, ?>> IRON_GALLIFREY_ORES_SMALL_KEY = registerKey("iron_gallifrey_ores_small");
    public static final ResourceKey<ConfiguredFeature<?, ?>> COAL_GALLIFREY_ORES_KEY = registerKey("coal_gallifrey_ores");
    public static final ResourceKey<ConfiguredFeature<?, ?>> GRAVEL_GALLIFREY_ORES_KEY = registerKey("gravel_gallifrey_ores");
    public static final ResourceKey<ConfiguredFeature<?, ?>> COBBLED_LIMESTONE_GALLIFREY_ORES_KEY = registerKey("cobbled_limestone_gallifrey_ores");
    public static final ResourceKey<ConfiguredFeature<?, ?>> GALLIFREYAN_DRY_GRASS = registerKey("gallifreyan_dry_grass");
    public static final ResourceKey<ConfiguredFeature<?, ?>> GALLIFREYAN_GRASS = registerKey("gallifreyan_grass");
    public static final ResourceKey<ConfiguredFeature<?, ?>> GALLIFREYAN_TALL_GRASS = registerKey("gallifreyan_tall_grass");
    public static final ResourceKey<ConfiguredFeature<?, ?>> CADONWOOD_TREE = registerKey("cadonwood_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> FALLEN_CADONWOOD_TREE = registerKey("fallen_cadonwood_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> GALLIFREYAN_STONE_BOULDER = registerKey("gallifreyan_stone_boulder");
    public static final ResourceKey<ConfiguredFeature<?, ?>> GALLIFREYAN_ARCADIAN_SHALE_BOULDER = registerKey("gallifreyan_arcadian_shale_boulder");
    public static final ResourceKey<ConfiguredFeature<?, ?>> MOON_BOULDER = registerKey("moon_boulder");
    public static final ResourceKey<ConfiguredFeature<?, ?>> GALLIFREYAN_STONE_PATCH = registerKey("gallifreyan_stone_patch");
    public static final ResourceKey<ConfiguredFeature<?, ?>> GALLIFREYAN_MUDSTONE_PATCH = registerKey("gallifreyan_mudstone_patch");
    public static final ResourceKey<ConfiguredFeature<?, ?>> THORIVINE = registerKey("thorivine");

    public static void bootstrap(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        List<OreConfiguration.TargetBlockState> gallifreyIronTargets = List.of(
                OreConfiguration.target(new TagMatchTest(GCTags.Blocks.GALLIFREYAN_STONE_REPLACEABLES), GCBlocks.GALLIFREYAN_STONE_IRON_ORE.defaultBlockState()));

        List<OreConfiguration.TargetBlockState> gallifreyCoalTargets = List.of(
                OreConfiguration.target(new TagMatchTest(GCTags.Blocks.GALLIFREYAN_STONE_REPLACEABLES), GCBlocks.GALLIFREYAN_STONE_COAL_ORE.defaultBlockState()));

        List<OreConfiguration.TargetBlockState> gallifreyGravelTargets = List.of(
                OreConfiguration.target(new TagMatchTest(GCTags.Blocks.BASE_STONE_GALLIFREY), GCBlocks.GALLIFREYAN_GRAVEL.defaultBlockState()));

        List<OreConfiguration.TargetBlockState> gallifreyCobbledLimestoneTargets = List.of(
                OreConfiguration.target(new TagMatchTest(GCTags.Blocks.BASE_STONE_GALLIFREY), GCBlocks.COBBLED_LIMESTONE.defaultBlockState()));

        register(context, CADONWOOD_TREE, Feature.TREE, new TreeConfiguration.TreeConfigurationBuilder(
                BlockStateProvider.simple(GCBlocks.CADONWOOD_LOG),
                new StraightTrunkPlacer(4, 2, 0),
                BlockStateProvider.simple(GCBlocks.CADONWOOD_LEAVES),
                new BlobFoliagePlacer(ConstantInt.of(2), ConstantInt.of(0), 3),
                new TwoLayersFeatureSize(1, 0, 1),
                BlockStateProvider.simple(GCBlocks.GALLIFREYAN_DIRT))
                .build());

        register(context, IRON_GALLIFREY_ORES_KEY, Feature.ORE, new OreConfiguration(gallifreyIronTargets, 9));
        register(context, IRON_GALLIFREY_ORES_SMALL_KEY, Feature.ORE, new OreConfiguration(gallifreyIronTargets, 4));
        register(context, COAL_GALLIFREY_ORES_KEY, Feature.ORE, new OreConfiguration(gallifreyCoalTargets, 17));
        register(context, GRAVEL_GALLIFREY_ORES_KEY, Feature.ORE, new OreConfiguration(gallifreyGravelTargets, 33));
        register(context, COBBLED_LIMESTONE_GALLIFREY_ORES_KEY, Feature.ORE, new OreConfiguration(gallifreyCobbledLimestoneTargets, 26));
        register(context, GALLIFREYAN_DRY_GRASS, Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(new WeightedStateProvider(WeightedList.<BlockState>builder()
                .add(GCBlocks.SHORT_GALLIFREYAN_DRY_GRASS.defaultBlockState(), 1)
                .add(GCBlocks.TALL_GALLIFREYAN_DRY_GRASS.defaultBlockState(), 1)
                .build())));
        register(context, GALLIFREYAN_GRASS, Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(BlockStateProvider.simple(GCBlocks.SHORT_GALLIFREYAN_GRASS)));
        register(context, GALLIFREYAN_TALL_GRASS, Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(BlockStateProvider.simple(GCBlocks.TALL_GALLIFREYAN_GRASS)));

        register(context, FALLEN_CADONWOOD_TREE, Feature.FALLEN_TREE, createFallenCadonwood().build());
        register(context, GALLIFREYAN_STONE_BOULDER, GCFeatures.BOULDER, new BoulderFeatureConfig(List.of(
                GCBlocks.GALLIFREYAN_STONE,
                GCBlocks.GALLIFREYAN_COBBLESTONE
        )));
        register(context, MOON_BOULDER, GCFeatures.BOULDER, new BoulderFeatureConfig(List.of(
                GCBlocks.LUNAR_REGOLITH,
                GCBlocks.MOONSLATE
        )));
        register(context, GALLIFREYAN_ARCADIAN_SHALE_BOULDER, GCFeatures.BOULDER, new BoulderFeatureConfig(List.of(
                GCBlocks.ARCADIAN_SHALE,
                GCBlocks.GALLIFREYAN_MUD,
                GCBlocks.GALLIFREYAN_MUDSTONE
        )));
        register(context, GALLIFREYAN_STONE_PATCH, Feature.REPLACE_BLOBS,
                new ReplaceSphereConfiguration(
                        GCBlocks.GALLIFREYAN_SAND.defaultBlockState(),
                        GCBlocks.GALLIFREYAN_STONE.defaultBlockState(),
                        UniformInt.of(3, 5)
                ));

        register(context, GALLIFREYAN_MUDSTONE_PATCH, Feature.REPLACE_BLOBS,
                new ReplaceSphereConfiguration(
                        GCBlocks.ARCADIAN_SHALE.defaultBlockState(),
                        GCBlocks.GALLIFREYAN_MUDSTONE.defaultBlockState(),
                        UniformInt.of(5, 8)
                ));

        register(context, THORIVINE, Feature.BLOCK_COLUMN, new BlockColumnConfiguration(
                List.of(
                        BlockColumnConfiguration.layer(
                                BiasedToBottomInt.of(2, 5),
                                BlockStateProvider.simple(GCBlocks.THORIVINE)
                        ),
                        BlockColumnConfiguration.layer(
                                new WeightedListInt(WeightedList.<IntProvider>builder()
                                        .add(ConstantInt.of(0), 5)
                                        .add(ConstantInt.of(1), 1)
                                        .build()),
                                BlockStateProvider.simple(GCBlocks.THORIVINE_FLOWER)
                        )
                ),
                Direction.UP,
                BlockPredicate.ONLY_IN_AIR_PREDICATE,
                false
        ));
    }

    private static FallenTreeConfiguration.FallenTreeConfigurationBuilder createFallenTrees() {
        return (new FallenTreeConfiguration.FallenTreeConfigurationBuilder(
                BlockStateProvider.simple(GCBlocks.CADONWOOD_LOG),
                UniformInt.of(4, 7)
        )).logDecorators(ImmutableList.of(
                new AttachedToLogsDecorator(
                        0.1F,
                        new WeightedStateProvider(
                                WeightedList.<BlockState>builder()
                                        .add(Blocks.RED_MUSHROOM.defaultBlockState(), 2)
                                        .add(Blocks.BROWN_MUSHROOM.defaultBlockState(), 1)
                                        .build()
                        ),
                        List.of(Direction.UP)
                )
        ));
    }

    private static FallenTreeConfiguration.FallenTreeConfigurationBuilder createFallenCadonwood() {
        return createFallenTrees()
                .stumpDecorators(ImmutableList.of(
                        new CadonwoodTrunkVineDecorator(
                                GCBlocks.GALLIFREYAN_VINE.defaultBlockState(),
                                0.5f
                        )
                ));
    }

    public static ResourceKey<ConfiguredFeature<?, ?>> registerKey(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, GCUtils.of(name));
    }

    private static <FC extends FeatureConfiguration, F extends Feature<FC>> void register(BootstrapContext<ConfiguredFeature<?, ?>> context,
                                                                                          ResourceKey<ConfiguredFeature<?, ?>> key, F feature, FC configuration) {
        context.register(key, new ConfiguredFeature<>(feature, configuration));
    }
}
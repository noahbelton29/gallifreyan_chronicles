package com.noahtnt2009.gallifreyan_chronicles.world;

import com.noahtnt2009.gallifreyan_chronicles.GallifreyanChronicles;
import com.noahtnt2009.gallifreyan_chronicles.registry.GCBlocks;
import com.noahtnt2009.gallifreyan_chronicles.registry.GCTags;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.random.WeightedList;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.AcaciaFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.BlobFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;
import net.minecraft.world.level.levelgen.feature.trunkplacers.BendingTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.StraightTrunkPlacer;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

import java.util.List;

public class GCConfiguredFeatures {
    public static final ResourceKey<ConfiguredFeature<?, ?>> IRON_GALLIFREY_ORES_KEY = registerKey("iron_gallifrey_ores");
    public static final ResourceKey<ConfiguredFeature<?, ?>> IRON_GALLIFREY_ORES_SMALL_KEY = registerKey("iron_gallifrey_ores_small");
    public static final ResourceKey<ConfiguredFeature<?, ?>> COAL_GALLIFREY_ORES_KEY = registerKey("coal_gallifrey_ores");
    public static final ResourceKey<ConfiguredFeature<?, ?>> GRAVEL_GALLIFREY_ORES_KEY = registerKey("gravel_gallifrey_ores");
    public static final ResourceKey<ConfiguredFeature<?, ?>> GALLIFREYAN_DRY_GRASS = registerKey("gallifreyan_dry_grass");
    public static final ResourceKey<ConfiguredFeature<?, ?>> GALLIFREYAN_GRASS = registerKey("gallifreyan_grass");
    public static final ResourceKey<ConfiguredFeature<?, ?>> GALLIFREYAN_TALL_GRASS = registerKey("gallifreyan_tall_grass");
    public static final ResourceKey<ConfiguredFeature<?, ?>> CADONWOOD_TREE = registerKey("cadonwood_tree");

    public static void bootstrap(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        List<OreConfiguration.TargetBlockState> gallifreyIronTargets = List.of(
                OreConfiguration.target(new TagMatchTest(GCTags.Blocks.GALLIFREYAN_STONE_REPLACEABLES), GCBlocks.GALLIFREYAN_STONE_IRON_ORE.defaultBlockState()));

        List<OreConfiguration.TargetBlockState> gallifreyCoalTargets = List.of(
                OreConfiguration.target(new TagMatchTest(GCTags.Blocks.GALLIFREYAN_STONE_REPLACEABLES), GCBlocks.GALLIFREYAN_STONE_COAL_ORE.defaultBlockState()));

        List<OreConfiguration.TargetBlockState> gallifreyGravelTargets = List.of(
                OreConfiguration.target(new TagMatchTest(GCTags.Blocks.BASE_STONE_GALLIFREY), GCBlocks.GALLIFREYAN_GRAVEL.defaultBlockState()));

        register(context, CADONWOOD_TREE, Feature.TREE, new TreeConfiguration.TreeConfigurationBuilder(
                BlockStateProvider.simple(GCBlocks.CADONWOOD_LOG),
                new StraightTrunkPlacer(4, 2, 0),
                BlockStateProvider.simple(GCBlocks.CADONWOOD_LEAVES),
                new BlobFoliagePlacer(ConstantInt.of(2), ConstantInt.of(0), 3),
                new TwoLayersFeatureSize(1, 0, 1))
                .build());

        register(context, IRON_GALLIFREY_ORES_KEY, Feature.ORE, new OreConfiguration(gallifreyIronTargets, 9));
        register(context, IRON_GALLIFREY_ORES_SMALL_KEY, Feature.ORE, new OreConfiguration(gallifreyIronTargets, 4));
        register(context, COAL_GALLIFREY_ORES_KEY, Feature.ORE, new OreConfiguration(gallifreyCoalTargets, 17));
        register(context, GRAVEL_GALLIFREY_ORES_KEY, Feature.ORE, new OreConfiguration(gallifreyGravelTargets, 33));
        register(context, GALLIFREYAN_DRY_GRASS, Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(new WeightedStateProvider(WeightedList.<BlockState>builder()
                .add(GCBlocks.SHORT_GALLIFREYAN_DRY_GRASS.defaultBlockState(), 1)
                .add(GCBlocks.TALL_GALLIFREYAN_DRY_GRASS.defaultBlockState(), 1)
                .build())));
        register(context, GALLIFREYAN_GRASS, Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(BlockStateProvider.simple(GCBlocks.SHORT_GALLIFREYAN_GRASS)));
        register(context, GALLIFREYAN_TALL_GRASS, Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(BlockStateProvider.simple(GCBlocks.TALL_GALLIFREYAN_GRASS)));

        //         FeatureUtils.register(context, ORE_GRAVEL, Feature.ORE, new OreConfiguration(naturalStone, Blocks.GRAVEL.defaultBlockState(), 33));
        // FeatureUtils.register(context, DRY_GRASS, Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(new WeightedStateProvider(WeightedList.builder().add(Blocks.SHORT_DRY_GRASS.defaultBlockState(), 1).add(Blocks.TALL_DRY_GRASS.defaultBlockState(), 1)))
    }

    public static ResourceKey<ConfiguredFeature<?, ?>> registerKey(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, Identifier.fromNamespaceAndPath(GallifreyanChronicles.MOD_ID, name));
    }

    private static <FC extends FeatureConfiguration, F extends Feature<FC>> void register(BootstrapContext<ConfiguredFeature<?, ?>> context,
                                                                                          ResourceKey<ConfiguredFeature<?, ?>> key, F feature, FC configuration) {
        context.register(key, new ConfiguredFeature<>(feature, configuration));
    }
}
package com.noahtnt2009.gallifreyan_chronicles.world;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.init.GCBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.*;

import java.util.List;

public class GCPlacedFeatures {
    public static final ResourceKey<PlacedFeature> IRON_GALLIFREY_ORES_UPPER_KEY = registerKey("iron_gallifrey_ores_upper");
    public static final ResourceKey<PlacedFeature> IRON_GALLIFREY_ORES_MIDDLE_KEY = registerKey("iron_gallifrey_ores_middle");
    public static final ResourceKey<PlacedFeature> IRON_GALLIFREY_ORES_SMALL_KEY  = registerKey("iron_gallifrey_ores_small");
    public static final ResourceKey<PlacedFeature> COAL_GALLIFREY_ORES_UPPER_KEY  = registerKey("coal_gallifrey_ores_upper");
    public static final ResourceKey<PlacedFeature> COAL_GALLIFREY_ORES_LOWER_KEY  = registerKey("coal_gallifrey_ores_lower");
    public static final ResourceKey<PlacedFeature> GRAVEL_GALLIFREY_ORES_KEY  = registerKey("gravel_gallifrey_ores");
    public static final ResourceKey<PlacedFeature> COBBLED_LIMESTONE_GALLIFREY_ORES_KEY  = registerKey("cobbled_limestone_gallifrey_ores");
    public static final ResourceKey<PlacedFeature> PATCH_GALLIFREY_DRY_GRASS  = registerKey("patch_gallifrey_dry_grass");
    public static final ResourceKey<PlacedFeature> PATCH_GALLIFREY_GRASS_PLAIN = registerKey("patch_gallifrey_grass_plain");
    public static final ResourceKey<PlacedFeature> PATCH_GALLIFREY_TALL_GRASS_PLAIN = registerKey("patch_gallifrey_tall_grass_plain");
    public static final ResourceKey<PlacedFeature> PATCH_GALLIFREY_TALL_GRASS_PLAIN_2 = registerKey("patch_gallifrey_tall_grass_plain_2");
    public static final ResourceKey<PlacedFeature> CADONWOOD_TREE_PLACED = registerKey("cadonwood_tree_placed");
    public static final ResourceKey<PlacedFeature> CADONWOOD_FALLEN_LOG = registerKey("cadonwood_fallen_log");
    public static final ResourceKey<PlacedFeature> GALLIFREYAN_STONE_BOULDER = registerKey("gallifreyan_stone_boulder");
    public static final ResourceKey<PlacedFeature> GALLIFREYAN_ARCADIAN_SHALE_BOULDER = registerKey("gallifreyan_arcadian_shale_boulder");
    public static final ResourceKey<PlacedFeature> MOON_BOULDER = registerKey("moon_boulder");
    public static final ResourceKey<PlacedFeature> GALLIFREYAN_STONE_PATCH = registerKey("gallifreyan_stone_patch");
    public static final ResourceKey<PlacedFeature> GALLIFREYAN_MUDSTONE_PATCH = registerKey("gallifreyan_mudstone_patch");
    public static final ResourceKey<PlacedFeature> PATCH_THORIVINE = registerKey("patch_thorivine");

    public static void bootstrap(BootstrapContext<PlacedFeature> context) {
        var configuredFeatures = context.lookup(Registries.CONFIGURED_FEATURE);
        var oreIron = configuredFeatures.getOrThrow(GCConfiguredFeatures.IRON_GALLIFREY_ORES_KEY);
        var oreIronSmall = configuredFeatures.getOrThrow(GCConfiguredFeatures.IRON_GALLIFREY_ORES_SMALL_KEY);
        var oreCoal = configuredFeatures.getOrThrow(GCConfiguredFeatures.COAL_GALLIFREY_ORES_KEY);
        var oreGravel = configuredFeatures.getOrThrow(GCConfiguredFeatures.GRAVEL_GALLIFREY_ORES_KEY);
        var oreCobbledLimestone = configuredFeatures.getOrThrow(GCConfiguredFeatures.COBBLED_LIMESTONE_GALLIFREY_ORES_KEY);
        var patchDryGrass = configuredFeatures.getOrThrow(GCConfiguredFeatures.GALLIFREYAN_DRY_GRASS);
        var grass = configuredFeatures.getOrThrow(GCConfiguredFeatures.GALLIFREYAN_GRASS);
        var tallGrass = configuredFeatures.getOrThrow(GCConfiguredFeatures.GALLIFREYAN_TALL_GRASS);
        var cadonwoodFallenLog = configuredFeatures.getOrThrow(GCConfiguredFeatures.FALLEN_CADONWOOD_TREE);
        var gallifreyanBoulder = configuredFeatures.getOrThrow(GCConfiguredFeatures.GALLIFREYAN_STONE_BOULDER);
        var moonBoulder = configuredFeatures.getOrThrow(GCConfiguredFeatures.MOON_BOULDER);
        var gallifreyanStonePatch = configuredFeatures.getOrThrow(GCConfiguredFeatures.GALLIFREYAN_STONE_PATCH);
        var gallifreyanMudstonePatch = configuredFeatures.getOrThrow(GCConfiguredFeatures.GALLIFREYAN_MUDSTONE_PATCH);
        var gallifreyanArcadianShaleBoulder = configuredFeatures.getOrThrow(GCConfiguredFeatures.GALLIFREYAN_ARCADIAN_SHALE_BOULDER);
        var thorivine = configuredFeatures.getOrThrow(GCConfiguredFeatures.THORIVINE);

        register(context, PATCH_THORIVINE, thorivine,
                RarityFilter.onAverageOnceEvery(6),
                InSquarePlacement.spread(),
                PlacementUtils.HEIGHTMAP,
                BiomeFilter.biome(),
                CountPlacement.of(10),
                RandomOffsetPlacement.ofTriangle(7, 3),
                BlockPredicateFilter.forPredicate(BlockPredicate.allOf(
                        BlockPredicate.ONLY_IN_AIR_PREDICATE,
                        BlockPredicate.wouldSurvive(GCBlocks.THORIVINE.defaultBlockState(), BlockPos.ZERO)
                ))
        );

        register(context, GALLIFREYAN_STONE_PATCH, gallifreyanStonePatch,
                RarityFilter.onAverageOnceEvery(8),
                InSquarePlacement.spread(),
                PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
                BiomeFilter.biome()
        );

        register(context, GALLIFREYAN_MUDSTONE_PATCH, gallifreyanMudstonePatch,
                RarityFilter.onAverageOnceEvery(14),
                InSquarePlacement.spread(),
                PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
                BiomeFilter.biome()
        );

        register(context, GALLIFREYAN_STONE_BOULDER, gallifreyanBoulder,
                RarityFilter.onAverageOnceEvery(12),
                InSquarePlacement.spread(),
                PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
                BiomeFilter.biome()
        );

        register(context, MOON_BOULDER, moonBoulder,
                RarityFilter.onAverageOnceEvery(15),
                InSquarePlacement.spread(),
                PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
                BiomeFilter.biome()
        );

        register(context, GALLIFREYAN_ARCADIAN_SHALE_BOULDER, gallifreyanArcadianShaleBoulder,
                RarityFilter.onAverageOnceEvery(16),
                InSquarePlacement.spread(),
                PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
                BiomeFilter.biome()
        );

        register(context, PATCH_GALLIFREY_GRASS_PLAIN, grass,
                NoiseThresholdCountPlacement.of(-0.8, 5, 10),
                InSquarePlacement.spread(),
                PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
                BiomeFilter.biome(),
                CountPlacement.of(32),
                RandomOffsetPlacement.ofTriangle(7, 3),
                BlockPredicateFilter.forPredicate(BlockPredicate.ONLY_IN_AIR_PREDICATE)
        );

        register(context, IRON_GALLIFREY_ORES_UPPER_KEY,  oreIron,
                GCOrePlacements.commonOrePlacement(90, HeightRangePlacement.triangle(VerticalAnchor.absolute(80),  VerticalAnchor.absolute(384))));
        register(context, IRON_GALLIFREY_ORES_MIDDLE_KEY, oreIron,
                GCOrePlacements.commonOrePlacement(10, HeightRangePlacement.triangle(VerticalAnchor.absolute(-24), VerticalAnchor.absolute(56))));
        register(context, IRON_GALLIFREY_ORES_SMALL_KEY,  oreIronSmall,
                GCOrePlacements.commonOrePlacement(10, HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(72))));

        register(context, COAL_GALLIFREY_ORES_UPPER_KEY, oreCoal,
                GCOrePlacements.commonOrePlacement(30, HeightRangePlacement.uniform(VerticalAnchor.absolute(136), VerticalAnchor.top())));
        register(context, COAL_GALLIFREY_ORES_LOWER_KEY, oreCoal,
                GCOrePlacements.commonOrePlacement(20, HeightRangePlacement.triangle(VerticalAnchor.absolute(0), VerticalAnchor.absolute(192))));

        register(context, GRAVEL_GALLIFREY_ORES_KEY, oreGravel,
                GCOrePlacements.commonOrePlacement(14, HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.top())));

        register(context, COBBLED_LIMESTONE_GALLIFREY_ORES_KEY, oreCobbledLimestone,
                GCOrePlacements.commonOrePlacement(12, HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.top())));

        register(context, PATCH_GALLIFREY_DRY_GRASS, patchDryGrass,
                RarityFilter.onAverageOnceEvery(10),
                InSquarePlacement.spread(),
                PlacementUtils.HEIGHTMAP,
                BiomeFilter.biome(),
                CountPlacement.of(48),
                RandomOffsetPlacement.ofTriangle(5, 2),
                BlockPredicateFilter.forPredicate(BlockPredicate.ONLY_IN_AIR_PREDICATE));

        register(context, PATCH_GALLIFREY_TALL_GRASS_PLAIN, tallGrass,
                RarityFilter.onAverageOnceEvery(5),
                InSquarePlacement.spread(),
                PlacementUtils.HEIGHTMAP,
                BiomeFilter.biome(),
                CountPlacement.of(96),
                RandomOffsetPlacement.ofTriangle(7, 3),
                BlockPredicateFilter.forPredicate(BlockPredicate.ONLY_IN_AIR_PREDICATE));

        register(context, PATCH_GALLIFREY_TALL_GRASS_PLAIN_2, tallGrass,
                NoiseThresholdCountPlacement.of(-0.8, 0, 7),
                RarityFilter.onAverageOnceEvery(32),
                InSquarePlacement.spread(),
                PlacementUtils.HEIGHTMAP,
                BiomeFilter.biome(),
                CountPlacement.of(96),
                RandomOffsetPlacement.ofTriangle(7, 3),
                BlockPredicateFilter.forPredicate(BlockPredicate.ONLY_IN_AIR_PREDICATE));

        register(context, CADONWOOD_TREE_PLACED, configuredFeatures.getOrThrow(GCConfiguredFeatures.CADONWOOD_TREE),
                VegetationPlacements.treePlacement(
                        PlacementUtils.countExtra(2, 0.1f, 2), GCBlocks.CADONWOOD_SAPLING));

        register(context, CADONWOOD_FALLEN_LOG, cadonwoodFallenLog,
                RarityFilter.onAverageOnceEvery(4),
                InSquarePlacement.spread(),
                PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
                BiomeFilter.biome(),
                CountPlacement.of(2)
        );
    }

    public static ResourceKey<PlacedFeature> registerKey(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, Identifier.fromNamespaceAndPath(Constants.MOD_ID, name));
    }

    private static <FC extends FeatureConfiguration, F extends Feature<FC>> void register(BootstrapContext<PlacedFeature> context, ResourceKey<PlacedFeature> key,
                                                                                          Holder<ConfiguredFeature<?, ?>> configuration, PlacementModifier... modifiers) {
        register(context, key, configuration, List.of(modifiers));
    }

    private static void register(BootstrapContext<PlacedFeature> context, ResourceKey<PlacedFeature> key, Holder<ConfiguredFeature<?, ?>> configuration,
                                 List<PlacementModifier> modifiers) {
        context.register(key, new PlacedFeature(configuration, List.copyOf(modifiers)));
    }
}
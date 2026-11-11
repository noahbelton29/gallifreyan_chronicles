package com.noahtnt2009.gallifreyan_chronicles.world.biome;

import com.noahtnt2009.gallifreyan_chronicles.init.GCBiomes;
import com.noahtnt2009.gallifreyan_chronicles.init.GCBlocks;
import net.minecraft.core.HolderGetter;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.material.MaterialRules;
import net.minecraft.world.level.levelgen.material.condition.MaterialCondition;
import net.minecraft.world.level.levelgen.material.rule.MaterialRule;
import net.minecraft.world.level.levelgen.placement.CaveSurface;

public class GCMaterialRules {
    private static final MaterialCondition ON_FLOOR = MaterialRules.stoneDepthCheck(0, false, CaveSurface.FLOOR);
    private static final MaterialCondition UNDER_FLOOR = MaterialRules.not(ON_FLOOR);
    private static final MaterialRule BEDROCK_FLOOR = MaterialRules.ifTrue(
            MaterialRules.verticalGradient("gallifreyan_chronicles:bedrock_floor", VerticalAnchor.bottom(), VerticalAnchor.aboveBottom(5)),
            block(Blocks.BEDROCK));

    private static MaterialRule block(Block block) {
        return MaterialRules.state(block.defaultBlockState());
    }

    private static MaterialRule belowYZero(Block block) {
        return MaterialRules.ifTrue(MaterialRules.not(MaterialRules.yBlockCheck(VerticalAnchor.absolute(0), 0)), block(block));
    }

    private static MaterialRule onFloor(Block block) {
        return MaterialRules.ifTrue(ON_FLOOR, block(block));
    }

    private static MaterialRule underFloor(Block block) {
        return MaterialRules.ifTrue(UNDER_FLOOR, block(block));
    }

    private static MaterialRule withinFloorDepth(int depth, Block block) {
        return MaterialRules.ifTrue(MaterialRules.stoneDepthCheck(depth, false, CaveSurface.FLOOR), block(block));
    }

    private static MaterialRule layerFrom(int y, Block block) {
        return MaterialRules.ifTrue(MaterialRules.yBlockCheck(VerticalAnchor.absolute(y), 0), block(block));
    }

    public static MaterialRule gallifrey(HolderGetter<Biome> biomes) {
        return MaterialRules.sequence(
                BEDROCK_FLOOR,
                belowYZero(GCBlocks.ARCADIAN_SHALE),
                MaterialRules.ifTrue(
                        MaterialRules.abovePreliminarySurface(),
                        MaterialRules.sequence(
                                MaterialRules.ifTrue(
                                        MaterialRules.isBiome(biomes, GCBiomes.GALLIFREYAN_PLAINS),
                                        MaterialRules.sequence(
                                                onFloor(GCBlocks.GALLIFREYAN_GRASS_BLOCK),
                                                withinFloorDepth(4, GCBlocks.GALLIFREYAN_DIRT),
                                                withinFloorDepth(8, GCBlocks.GALLIFREYAN_MUD)
                                        )
                                ),
                                MaterialRules.ifTrue(
                                        MaterialRules.isBiome(biomes, GCBiomes.GALLIFREYAN_DESERT),
                                        MaterialRules.sequence(
                                                onFloor(GCBlocks.GALLIFREYAN_SAND),
                                                underFloor(GCBlocks.GALLIFREYAN_SAND)
                                        )
                                ),
                                MaterialRules.ifTrue(
                                        MaterialRules.isBiome(biomes, GCBiomes.GALLIFREYAN_BADLANDS),
                                        MaterialRules.sequence(
                                                onFloor(GCBlocks.ARCADIAN_SHALE),
                                                withinFloorDepth(1, GCBlocks.ARCADIAN_SHALE),
                                                layerFrom(120, GCBlocks.GALLIFREYAN_GRAVEL),
                                                layerFrom(112, GCBlocks.GALLIFREYAN_MUDSTONE),
                                                layerFrom(104, GCBlocks.ARCADIAN_SHALE),
                                                layerFrom(96, GCBlocks.GALLIFREYAN_MUD),
                                                layerFrom(88, GCBlocks.GALLIFREYAN_MUDSTONE),
                                                layerFrom(80, GCBlocks.ARCADIAN_SHALE),
                                                layerFrom(72, GCBlocks.GALLIFREYAN_GRAVEL),
                                                layerFrom(64, GCBlocks.GALLIFREYAN_MUD),
                                                block(GCBlocks.ARCADIAN_SHALE)
                                        )
                                )
                        )
                )
        );
    }

    public static MaterialRule moon(HolderGetter<Biome> biomes) {
        return MaterialRules.sequence(
                BEDROCK_FLOOR,
                belowYZero(GCBlocks.MOONSLATE),
                MaterialRules.ifTrue(
                        MaterialRules.abovePreliminarySurface(),
                        MaterialRules.sequence(
                                MaterialRules.ifTrue(
                                        MaterialRules.isBiome(biomes, GCBiomes.MOON),
                                        MaterialRules.sequence(
                                                onFloor(GCBlocks.MOON_DUST),
                                                withinFloorDepth(4, GCBlocks.LUNAR_REGOLITH)
                                        )
                                )
                        )
                )
        );
    }
}
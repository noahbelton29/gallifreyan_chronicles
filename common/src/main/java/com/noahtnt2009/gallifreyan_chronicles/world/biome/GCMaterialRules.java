package com.noahtnt2009.gallifreyan_chronicles.world.biome;

import com.noahtnt2009.gallifreyan_chronicles.init.GCBiomes;
import com.noahtnt2009.gallifreyan_chronicles.init.GCBlocks;
import net.minecraft.core.HolderGetter;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.material.MaterialRules;
import net.minecraft.world.level.levelgen.material.condition.MaterialCondition;
import net.minecraft.world.level.levelgen.material.rule.MaterialRule;
import net.minecraft.world.level.levelgen.placement.CaveSurface;

public class GCMaterialRules {
    private static final MaterialCondition ON_FLOOR = MaterialRules.stoneDepthCheck(0, false, CaveSurface.FLOOR);
    private static final MaterialCondition UNDER_FLOOR = MaterialRules.not(ON_FLOOR);

    public static MaterialRule gallifrey(HolderGetter<Biome> biomes) {
        return MaterialRules.sequence(
                MaterialRules.ifTrue(
                        MaterialRules.verticalGradient("gallifreyan_chronicles:bedrock_floor", VerticalAnchor.bottom(), VerticalAnchor.aboveBottom(5)),
                        MaterialRules.state(Blocks.BEDROCK.defaultBlockState())
                ),
                MaterialRules.ifTrue(
                        MaterialRules.not(MaterialRules.yBlockCheck(VerticalAnchor.absolute(0), 0)),
                        MaterialRules.state(GCBlocks.ARCADIAN_SHALE.defaultBlockState())
                ),
                MaterialRules.ifTrue(
                        MaterialRules.abovePreliminarySurface(),
                        MaterialRules.sequence(
                                MaterialRules.ifTrue(
                                        MaterialRules.isBiome(biomes, GCBiomes.GALLIFREYAN_PLAINS),
                                        MaterialRules.sequence(
                                                MaterialRules.ifTrue(
                                                        ON_FLOOR,
                                                        MaterialRules.state(GCBlocks.GALLIFREYAN_GRASS_BLOCK.defaultBlockState())
                                                ),
                                                MaterialRules.ifTrue(
                                                        MaterialRules.stoneDepthCheck(4, false, CaveSurface.FLOOR),
                                                        MaterialRules.state(GCBlocks.GALLIFREYAN_DIRT.defaultBlockState())
                                                ),
                                                MaterialRules.ifTrue(
                                                        MaterialRules.stoneDepthCheck(8, false, CaveSurface.FLOOR),
                                                        MaterialRules.state(GCBlocks.GALLIFREYAN_MUD.defaultBlockState())
                                                )
                                        )
                                ),
                                MaterialRules.ifTrue(
                                        MaterialRules.isBiome(biomes, GCBiomes.GALLIFREYAN_DESERT),
                                        MaterialRules.sequence(
                                                MaterialRules.ifTrue(
                                                        ON_FLOOR,
                                                        MaterialRules.state(GCBlocks.GALLIFREYAN_SAND.defaultBlockState())
                                                ),
                                                MaterialRules.ifTrue(
                                                        UNDER_FLOOR,
                                                        MaterialRules.state(GCBlocks.GALLIFREYAN_SAND.defaultBlockState())
                                                )
                                        )
                                ),
                                MaterialRules.ifTrue(
                                        MaterialRules.isBiome(biomes, GCBiomes.GALLIFREYAN_BADLANDS),
                                        MaterialRules.sequence(
                                                MaterialRules.ifTrue(
                                                        ON_FLOOR,
                                                        MaterialRules.state(GCBlocks.ARCADIAN_SHALE.defaultBlockState())
                                                ),
                                                MaterialRules.ifTrue(
                                                        MaterialRules.stoneDepthCheck(1, false, CaveSurface.FLOOR),
                                                        MaterialRules.state(GCBlocks.ARCADIAN_SHALE.defaultBlockState())
                                                ),
                                                MaterialRules.ifTrue(
                                                        MaterialRules.yBlockCheck(VerticalAnchor.absolute(120), 0),
                                                        MaterialRules.state(GCBlocks.GALLIFREYAN_GRAVEL.defaultBlockState())
                                                ),
                                                MaterialRules.ifTrue(
                                                        MaterialRules.yBlockCheck(VerticalAnchor.absolute(112), 0),
                                                        MaterialRules.state(GCBlocks.GALLIFREYAN_MUDSTONE.defaultBlockState())
                                                ),
                                                MaterialRules.ifTrue(
                                                        MaterialRules.yBlockCheck(VerticalAnchor.absolute(104), 0),
                                                        MaterialRules.state(GCBlocks.ARCADIAN_SHALE.defaultBlockState())
                                                ),
                                                MaterialRules.ifTrue(
                                                        MaterialRules.yBlockCheck(VerticalAnchor.absolute(96), 0),
                                                        MaterialRules.state(GCBlocks.GALLIFREYAN_MUD.defaultBlockState())
                                                ),
                                                MaterialRules.ifTrue(
                                                        MaterialRules.yBlockCheck(VerticalAnchor.absolute(88), 0),
                                                        MaterialRules.state(GCBlocks.GALLIFREYAN_MUDSTONE.defaultBlockState())
                                                ),
                                                MaterialRules.ifTrue(
                                                        MaterialRules.yBlockCheck(VerticalAnchor.absolute(80), 0),
                                                        MaterialRules.state(GCBlocks.ARCADIAN_SHALE.defaultBlockState())
                                                ),
                                                MaterialRules.ifTrue(
                                                        MaterialRules.yBlockCheck(VerticalAnchor.absolute(72), 0),
                                                        MaterialRules.state(GCBlocks.GALLIFREYAN_GRAVEL.defaultBlockState())
                                                ),
                                                MaterialRules.ifTrue(
                                                        MaterialRules.yBlockCheck(VerticalAnchor.absolute(64), 0),
                                                        MaterialRules.state(GCBlocks.GALLIFREYAN_MUD.defaultBlockState())
                                                ),
                                                MaterialRules.state(GCBlocks.ARCADIAN_SHALE.defaultBlockState())
                                        )
                                )
                        )
                )
        );
    }

    public static MaterialRule moon(HolderGetter<Biome> biomes) {
        return MaterialRules.sequence(
                MaterialRules.ifTrue(
                        MaterialRules.verticalGradient("gallifreyan_chronicles:bedrock_floor", VerticalAnchor.bottom(), VerticalAnchor.aboveBottom(5)),
                        MaterialRules.state(Blocks.BEDROCK.defaultBlockState())
                ),
                MaterialRules.ifTrue(
                        MaterialRules.not(MaterialRules.yBlockCheck(VerticalAnchor.absolute(0), 0)),
                        MaterialRules.state(GCBlocks.MOONSLATE.defaultBlockState())
                ),
                MaterialRules.ifTrue(
                        MaterialRules.abovePreliminarySurface(),
                        MaterialRules.sequence(
                                MaterialRules.ifTrue(
                                        MaterialRules.isBiome(biomes, GCBiomes.MOON),
                                        MaterialRules.sequence(
                                                MaterialRules.ifTrue(
                                                        ON_FLOOR,
                                                        MaterialRules.state(GCBlocks.MOON_DUST.defaultBlockState())
                                                ),
                                                MaterialRules.ifTrue(
                                                        MaterialRules.stoneDepthCheck(4, false, CaveSurface.FLOOR),
                                                        MaterialRules.state(GCBlocks.LUNAR_REGOLITH.defaultBlockState())
                                                )
                                        )
                                )
                        )
                )
        );
    }
}
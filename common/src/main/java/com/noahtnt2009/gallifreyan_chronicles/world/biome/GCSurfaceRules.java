package com.noahtnt2009.gallifreyan_chronicles.world.biome;

import com.noahtnt2009.gallifreyan_chronicles.init.GCBiomes;
import com.noahtnt2009.gallifreyan_chronicles.init.GCBlocks;
import net.minecraft.core.HolderGetter;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.placement.CaveSurface;

public class GCSurfaceRules {
    public static SurfaceRules.RuleSource gallifrey(HolderGetter<Biome> biomes) {
        return SurfaceRules.sequence(
                SurfaceRules.ifTrue(
                        SurfaceRules.verticalGradient("gallifreyan_chronicles:bedrock_floor", VerticalAnchor.bottom(), VerticalAnchor.aboveBottom(5)),
                        SurfaceRules.state(Blocks.BEDROCK.defaultBlockState())
                ),
                SurfaceRules.ifTrue(
                        SurfaceRules.not(SurfaceRules.yBlockCheck(VerticalAnchor.absolute(0), 0)),
                        SurfaceRules.state(GCBlocks.ARCADIAN_SHALE.defaultBlockState())
                ),
                SurfaceRules.ifTrue(
                        SurfaceRules.abovePreliminarySurface(),
                        SurfaceRules.sequence(
                                SurfaceRules.ifTrue(
                                        SurfaceRules.isBiome(biomes, GCBiomes.GALLIFREYAN_PLAINS),
                                        SurfaceRules.sequence(
                                                SurfaceRules.ifTrue(
                                                        SurfaceRules.ON_FLOOR,
                                                        SurfaceRules.state(GCBlocks.GALLIFREYAN_GRASS_BLOCK.defaultBlockState())
                                                ),
                                                SurfaceRules.ifTrue(
                                                        SurfaceRules.stoneDepthCheck(4, false, CaveSurface.FLOOR),
                                                        SurfaceRules.state(GCBlocks.GALLIFREYAN_DIRT.defaultBlockState())
                                                ),
                                                SurfaceRules.ifTrue(
                                                        SurfaceRules.stoneDepthCheck(8, false, CaveSurface.FLOOR),
                                                        SurfaceRules.state(GCBlocks.GALLIFREYAN_MUD.defaultBlockState())
                                                )
                                        )
                                ),
                                SurfaceRules.ifTrue(
                                        SurfaceRules.isBiome(biomes, GCBiomes.GALLIFREYAN_DESERT),
                                        SurfaceRules.sequence(
                                                SurfaceRules.ifTrue(
                                                        SurfaceRules.ON_FLOOR,
                                                        SurfaceRules.state(GCBlocks.GALLIFREYAN_SAND.defaultBlockState())
                                                ),
                                                SurfaceRules.ifTrue(
                                                        SurfaceRules.UNDER_FLOOR,
                                                        SurfaceRules.state(GCBlocks.GALLIFREYAN_SAND.defaultBlockState())
                                                )
                                        )
                                ),
                                SurfaceRules.ifTrue(
                                        SurfaceRules.isBiome(biomes, GCBiomes.GALLIFREYAN_BADLANDS),
                                        SurfaceRules.sequence(
                                                SurfaceRules.ifTrue(
                                                        SurfaceRules.ON_FLOOR,
                                                        SurfaceRules.state(GCBlocks.ARCADIAN_SHALE.defaultBlockState())
                                                ),
                                                SurfaceRules.ifTrue(
                                                        SurfaceRules.stoneDepthCheck(1, false, CaveSurface.FLOOR),
                                                        SurfaceRules.state(GCBlocks.ARCADIAN_SHALE.defaultBlockState())
                                                ),
                                                SurfaceRules.ifTrue(
                                                        SurfaceRules.yBlockCheck(VerticalAnchor.absolute(120), 0),
                                                        SurfaceRules.state(GCBlocks.GALLIFREYAN_GRAVEL.defaultBlockState())
                                                ),
                                                SurfaceRules.ifTrue(
                                                        SurfaceRules.yBlockCheck(VerticalAnchor.absolute(112), 0),
                                                        SurfaceRules.state(GCBlocks.GALLIFREYAN_MUDSTONE.defaultBlockState())
                                                ),
                                                SurfaceRules.ifTrue(
                                                        SurfaceRules.yBlockCheck(VerticalAnchor.absolute(104), 0),
                                                        SurfaceRules.state(GCBlocks.ARCADIAN_SHALE.defaultBlockState())
                                                ),
                                                SurfaceRules.ifTrue(
                                                        SurfaceRules.yBlockCheck(VerticalAnchor.absolute(96), 0),
                                                        SurfaceRules.state(GCBlocks.GALLIFREYAN_MUD.defaultBlockState())
                                                ),
                                                SurfaceRules.ifTrue(
                                                        SurfaceRules.yBlockCheck(VerticalAnchor.absolute(88), 0),
                                                        SurfaceRules.state(GCBlocks.GALLIFREYAN_MUDSTONE.defaultBlockState())
                                                ),
                                                SurfaceRules.ifTrue(
                                                        SurfaceRules.yBlockCheck(VerticalAnchor.absolute(80), 0),
                                                        SurfaceRules.state(GCBlocks.ARCADIAN_SHALE.defaultBlockState())
                                                ),
                                                SurfaceRules.ifTrue(
                                                        SurfaceRules.yBlockCheck(VerticalAnchor.absolute(72), 0),
                                                        SurfaceRules.state(GCBlocks.GALLIFREYAN_GRAVEL.defaultBlockState())
                                                ),
                                                SurfaceRules.ifTrue(
                                                        SurfaceRules.yBlockCheck(VerticalAnchor.absolute(64), 0),
                                                        SurfaceRules.state(GCBlocks.GALLIFREYAN_MUD.defaultBlockState())
                                                ),
                                                // Default deep layer
                                                SurfaceRules.state(GCBlocks.ARCADIAN_SHALE.defaultBlockState())
                                        )
                                )
                        )
                )
        );
    }
    public static SurfaceRules.RuleSource moon(HolderGetter<Biome> biomes) {
        return SurfaceRules.sequence(
                SurfaceRules.ifTrue(
                        SurfaceRules.verticalGradient("gallifreyan_chronicles:bedrock_floor", VerticalAnchor.bottom(), VerticalAnchor.aboveBottom(5)),
                        SurfaceRules.state(Blocks.BEDROCK.defaultBlockState())
                ),
                SurfaceRules.ifTrue(
                        SurfaceRules.not(SurfaceRules.yBlockCheck(VerticalAnchor.absolute(0), 0)),
                        SurfaceRules.state(GCBlocks.MOONSLATE.defaultBlockState())
                ),
                SurfaceRules.ifTrue(
                        SurfaceRules.abovePreliminarySurface(),
                        SurfaceRules.sequence(
                                SurfaceRules.ifTrue(
                                        SurfaceRules.isBiome(biomes, GCBiomes.MOON),
                                        SurfaceRules.sequence(
                                                SurfaceRules.ifTrue(
                                                        SurfaceRules.ON_FLOOR,
                                                        SurfaceRules.state(GCBlocks.MOON_DUST.defaultBlockState())
                                                ),
                                                SurfaceRules.ifTrue(
                                                        SurfaceRules.stoneDepthCheck(4, false, CaveSurface.FLOOR),
                                                        SurfaceRules.state(GCBlocks.LUNAR_REGOLITH.defaultBlockState())
                                                )
                                        )
                                )
                        )
                )
        );
    }
}
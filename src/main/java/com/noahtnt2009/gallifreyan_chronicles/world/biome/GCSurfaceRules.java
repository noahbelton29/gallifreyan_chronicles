package com.noahtnt2009.gallifreyan_chronicles.world.biome;

import com.noahtnt2009.gallifreyan_chronicles.registry.GCBiomes;
import com.noahtnt2009.gallifreyan_chronicles.registry.GCBlocks;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.placement.CaveSurface;

public class GCSurfaceRules {
    public static SurfaceRules.RuleSource gallifrey() {
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
                                        SurfaceRules.isBiome(GCBiomes.GALLIFREYAN_PLAINS),
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
                                        SurfaceRules.isBiome(GCBiomes.GALLIFREYAN_DESERT),
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
                                )
                        )
                )
        );
    }
}
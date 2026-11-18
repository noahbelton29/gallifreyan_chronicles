package uk.nbforge.gallifreyan_chronicles.core.block.registry;

import static uk.nbforge.gallifreyan_chronicles.core.GCBlocks.register;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CactusFlowerBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.ShortDryGrassBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TallDryGrassBlock;
import net.minecraft.world.level.block.TallGrassBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import uk.nbforge.gallifreyan_chronicles.core.block.ThorivineBlock;

public final class FloraBlocks {
    private FloraBlocks() {}

    public static final Block GALLIFREYAN_VINE = register(
            "gallifreyan_vine",
            properties -> new VineBlock(properties
                    .mapColor(MapColor.PLANT)
                    .replaceable()
                    .noCollision()
                    .randomTicks()
                    .strength(0.2f)
                    .sound(SoundType.VINE)
                    .ignitedByLava()
                    .pushReaction(PushReaction.POPPED)));

    public static final Block THORIVINE = register(
            "thorivine",
            properties -> new ThorivineBlock(properties
                    .mapColor(MapColor.PLANT)
                    .randomTicks()
                    .strength(0.8f)
                    .sound(SoundType.WOOL)
                    .pushReaction(PushReaction.POPPED)));

    public static final Block THORIVINE_FLOWER = register(
            "thorivine_flower",
            properties -> new CactusFlowerBlock(properties
                    .mapColor(MapColor.COLOR_PINK)
                    .noCollision()
                    .instabreak()
                    .ignitedByLava()
                    .sound(SoundType.CACTUS_FLOWER)
                    .pushReaction(PushReaction.POPPED)));

    public static final Block SHORT_GALLIFREYAN_DRY_GRASS = register(
            "short_gallifreyan_dry_grass",
            properties -> new ShortDryGrassBlock(grass(properties, MapColor.COLOR_ORANGE)));
    public static final Block SHORT_GALLIFREYAN_GRASS = register(
            "short_gallifreyan_grass", properties -> new TallGrassBlock(grass(properties, MapColor.COLOR_YELLOW)));
    public static final Block TALL_GALLIFREYAN_GRASS = register(
            "tall_gallifreyan_grass", properties -> new DoublePlantBlock(grass(properties, MapColor.COLOR_YELLOW)));
    public static final Block TALL_GALLIFREYAN_DRY_GRASS = register(
            "tall_gallifreyan_dry_grass",
            properties -> new TallDryGrassBlock(grass(properties, MapColor.COLOR_ORANGE)));

    private static BlockBehaviour.Properties grass(BlockBehaviour.Properties properties, MapColor colour) {
        return properties
                .mapColor(colour)
                .replaceable()
                .noCollision()
                .instabreak()
                .sound(SoundType.GRASS)
                .ignitedByLava()
                .offsetType(BlockBehaviour.OffsetType.XYZ)
                .pushReaction(PushReaction.POPPED);
    }

    public static void init() {}
}

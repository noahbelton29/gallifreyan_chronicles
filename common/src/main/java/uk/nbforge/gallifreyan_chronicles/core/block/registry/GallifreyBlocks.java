package uk.nbforge.gallifreyan_chronicles.core.block.registry;

import static uk.nbforge.gallifreyan_chronicles.core.GCBlocks.register;

import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.SoundType;
import uk.nbforge.gallifreyan_chronicles.core.block.GCFallingBlock;

public final class GallifreyBlocks {
    private GallifreyBlocks() {}

    public static final Block GALLIFREYAN_SAND = register(
            "gallifreyan_sand",
            properties -> new GCFallingBlock(properties.strength(1.25f).sound(SoundType.SAND)));

    public static final Block GALLIFREYAN_GRAVEL = register(
            "gallifreyan_gravel",
            properties -> new GCFallingBlock(properties.strength(1.25f).sound(SoundType.GRAVEL)));

    public static final Block GALLIFREYAN_DIRT = register(
            "gallifreyan_dirt",
            properties -> new Block(properties.strength(0.6f).sound(SoundType.GRAVEL)));

    public static final Block GALLIFREYAN_GRASS_BLOCK = register(
            "gallifreyan_grass_block",
            properties -> new GrassBlock(properties.strength(0.7f).sound(SoundType.GRASS)));

    public static final Block GALLIFREYAN_STONE = register(
            "gallifreyan_stone",
            properties -> new Block(properties
                    .strength(4.5f, 8.5f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.STONE)));

    public static final Block COBBLED_LIMESTONE = register(
            "cobbled_limestone",
            properties -> new Block(properties
                    .strength(2.5f, 2.5f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.DRIPSTONE_BLOCK)));

    public static final Block GALLIFREYAN_STONE_IRON_ORE = register(
            "gallifreyan_stone_iron_ore",
            properties -> new DropExperienceBlock(
                    UniformInt.of(0, 2),
                    properties
                            .strength(4.5f, 8.5f)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.STONE)));

    public static final Block GALLIFREYAN_STONE_COAL_ORE = register(
            "gallifreyan_stone_coal_ore",
            properties -> new DropExperienceBlock(
                    UniformInt.of(2, 4),
                    properties
                            .strength(4.5f, 8.5f)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.STONE)));

    public static final Block GALLIFREYAN_COBBLESTONE = register(
            "gallifreyan_cobblestone",
            properties -> new Block(
                    properties.strength(4f, 6.5f).requiresCorrectToolForDrops().sound(SoundType.STONE)));

    public static final Block GALLIFREYAN_MUD = register(
            "gallifreyan_mud",
            properties -> new Block(
                    properties.strength(0.75f).requiresCorrectToolForDrops().sound(SoundType.ROOTED_DIRT)));

    public static final Block GALLIFREYAN_MUDSTONE = register(
            "gallifreyan_mudstone",
            properties -> new Block(
                    properties.strength(2.75f).requiresCorrectToolForDrops().sound(SoundType.STONE)));

    public static final Block ARCADIAN_SHALE = register(
            "arcadian_shale",
            properties -> new Block(properties
                    .strength(6.75f, 11.5f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.DEEPSLATE)));

    public static final Block ARCADIAN_SHALE_TARANIUM_ORE = register(
            "arcadian_shale_taranium_ore",
            properties -> new DropExperienceBlock(
                    UniformInt.of(7, 11),
                    properties
                            .strength(6.75f, 11.5f)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.DEEPSLATE)));

    public static void init() {}
}

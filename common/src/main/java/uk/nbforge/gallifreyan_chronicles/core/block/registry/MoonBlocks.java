package uk.nbforge.gallifreyan_chronicles.core.block.registry;

import static uk.nbforge.gallifreyan_chronicles.core.GCBlocks.register;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import uk.nbforge.gallifreyan_chronicles.core.block.GCFallingBlock;

public final class MoonBlocks {
    private MoonBlocks() {}

    public static final Block MOON_DUST = register(
            "moon_dust",
            properties -> new GCFallingBlock(properties.strength(1.25f).sound(SoundType.SAND)));

    public static final Block LUNAR_REGOLITH = register(
            "lunar_regolith",
            properties -> new Block(properties
                    .strength(3.5f, 3.5f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.STONE)));

    public static final Block MOONSLATE = register(
            "moonslate",
            properties -> new Block(properties
                    .strength(5.5f, 5.5f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.DEEPSLATE)));

    public static void init() {}
}

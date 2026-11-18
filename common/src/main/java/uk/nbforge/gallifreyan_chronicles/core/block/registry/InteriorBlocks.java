package uk.nbforge.gallifreyan_chronicles.core.block.registry;

import static uk.nbforge.gallifreyan_chronicles.core.GCBlocks.register;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import uk.nbforge.gallifreyan_chronicles.core.GCBlocks;

public final class InteriorBlocks {
    public static final Block BRACHACKI_FLOOR_BLOCK = stoneBlock("brachacki_floor_block");
    public static final Block BRACHACKI_WALL_BLOCK = stoneBlock("brachacki_wall_block");
    public static final Block BRACHACKI_ROUNDEL_BLOCK = stoneBlock("brachacki_roundel_block");
    public static final Block BRACHACKI_HALF_ROUNDEL_BLOCK = stoneBlock("brachacki_half_roundel_block");
    public static final Block BRACHACKI_ROUNDEL_ALT_BLOCK = stoneBlock("brachacki_roundel_alt_block");
    public static final Block BRACHACKI_HALF_ROUNDEL_ALT_BLOCK = stoneBlock("brachacki_half_roundel_alt_block");
    public static final Block BRACHACKI_WALL_BLOCK_2 = stoneBlock("brachacki_wall_block_2");
    public static final Block BRACHACKI_ROUNDEL_BLOCK_2 = stoneBlock("brachacki_roundel_block_2");
    public static final Block BRACHACKI_HALF_ROUNDEL_BLOCK_2 = stoneBlock("brachacki_half_roundel_block_2");
    public static final Block BRACHACKI_WALL_BLOCK_3 = stoneBlock("brachacki_wall_block_3");
    public static final Block BRACHACKI_WALL_BLOCK_4 = stoneBlock("brachacki_wall_block_4");
    public static final Block BRACHACKI_ROUNDEL_BLOCK_3 = stoneBlock("brachacki_roundel_block_3");
    public static final Block BRACHACKI_ROUNDEL_BLOCK_4 = stoneBlock("brachacki_roundel_block_4");
    public static final Block BRACHACKI_HALF_ROUNDEL_BLOCK_3 = stoneBlock("brachacki_half_roundel_block_3");
    public static final Block BRACHACKI_HALF_ROUNDEL_BLOCK_4 = stoneBlock("brachacki_half_roundel_block_4");
    public static final Block BRACHACKI_LIGHT_STRIP_BLOCK = stoneBlock("brachacki_light_strip_block");
    public static final Block VOID_BLOCK = register(
            "void_block",
            properties -> new Block(properties
                    .mapColor(MapColor.COLOR_BLACK)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .strength(1.8f)
                    .sound(SoundType.STONE)
                    .noCollision()
                    .isSuffocating(GCBlocks::never)
                    .isViewBlocking(GCBlocks::neverViewBlocking)));
    public static final Block BRACHACKI_TUAT_FLOOR_BLOCK = stoneBlock("brachacki_tuat_floor_block");
    public static final Block BRACHACKI_TUAT_WALL_BLOCK = stoneBlock("brachacki_tuat_wall_block");
    public static final Block BRACHACKI_TUAT_ROUNDEL_BLOCK = stoneBlock("brachacki_tuat_roundel_block");
    public static final Block BRACHACKI_TUAT_HALF_ROUNDEL_BLOCK = stoneBlock("brachacki_tuat_half_roundel_block");
    public static final Block BRACHACKI_TUAT_HEXAGONAL_ROUNDEL_BLOCK =
            stoneBlock("brachacki_tuat_hexagonal_roundel_block");
    public static final Block BRACHACKI_TUAT_HALF_HEXAGONAL_ROUNDEL_BLOCK =
            stoneBlock("brachacki_tuat_half_hexagonal_roundel_block");
    public static final Block BRACHACKI_STAIRS = stoneStairs("brachacki_stairs", BRACHACKI_FLOOR_BLOCK);
    public static final Block BRACHACKI_SLAB = stoneSlab("brachacki_slab");
    public static final Block BRACHACKI_PRESSURE_PLATE = stonePressurePlate("brachacki_pressure_plate");
    public static final Block BRACHACKI_WALL = stoneWall("brachacki_wall");
    public static final Block BRACHACKI_FENCE = stoneFence("brachacki_fence");
    public static final Block BRACHACKI_FENCE_GATE = stoneFenceGate("brachacki_fence_gate");
    public static final Block BRACHACKI_STAIRS_2 = stoneStairs("brachacki_stairs_2", BRACHACKI_FLOOR_BLOCK);
    public static final Block BRACHACKI_SLAB_2 = stoneSlab("brachacki_slab_2");
    public static final Block BRACHACKI_PRESSURE_PLATE_2 = stonePressurePlate("brachacki_pressure_plate_2");
    public static final Block BRACHACKI_WALL_2 = stoneWall("brachacki_wall_2");
    public static final Block BRACHACKI_FENCE_2 = stoneFence("brachacki_fence_2");
    public static final Block BRACHACKI_FENCE_GATE_2 = stoneFenceGate("brachacki_fence_gate_2");
    public static final Block BRACHACKI_STAIRS_3 = stoneStairs("brachacki_stairs_3", BRACHACKI_WALL_BLOCK_3);
    public static final Block BRACHACKI_STAIRS_4 = stoneStairs("brachacki_stairs_4", BRACHACKI_WALL_BLOCK_4);
    public static final Block BRACHACKI_SLAB_3 = stoneSlab("brachacki_slab_3");
    public static final Block BRACHACKI_SLAB_4 = stoneSlab("brachacki_slab_4");
    public static final Block BRACHACKI_PRESSURE_PLATE_3 = stonePressurePlate("brachacki_pressure_plate_3");
    public static final Block BRACHACKI_PRESSURE_PLATE_4 = stonePressurePlate("brachacki_pressure_plate_4");
    public static final Block BRACHACKI_WALL_3 = stoneWall("brachacki_wall_3");
    public static final Block BRACHACKI_WALL_4 = stoneWall("brachacki_wall_4");
    public static final Block BRACHACKI_FENCE_3 = stoneFence("brachacki_fence_3");
    public static final Block BRACHACKI_FENCE_4 = stoneFence("brachacki_fence_4");
    public static final Block BRACHACKI_FENCE_GATE_3 = stoneFenceGate("brachacki_fence_gate_3");
    public static final Block BRACHACKI_FENCE_GATE_4 = stoneFenceGate("brachacki_fence_gate_4");
    public static final Block BRACHACKI_TUAT_STAIRS = stoneStairs("brachacki_tuat_stairs", BRACHACKI_TUAT_WALL_BLOCK);
    public static final Block BRACHACKI_TUAT_SLAB = stoneSlab("brachacki_tuat_slab");
    public static final Block BRACHACKI_TUAT_PRESSURE_PLATE = stonePressurePlate("brachacki_tuat_pressure_plate");
    public static final Block BRACHACKI_TUAT_WALL = stoneWall("brachacki_tuat_wall");
    public static final Block BRACHACKI_TUAT_FENCE = stoneFence("brachacki_tuat_fence");
    public static final Block BRACHACKI_TUAT_FENCE_GATE = stoneFenceGate("brachacki_tuat_fence_gate");

    private InteriorBlocks() {}

    private static BlockBehaviour.Properties stone(BlockBehaviour.Properties properties) {
        return properties.strength(3f, 3f).requiresCorrectToolForDrops().sound(SoundType.STONE);
    }

    private static Block stoneBlock(String name) {
        return register(name, properties -> new Block(stone(properties)));
    }

    private static Block stoneStairs(String name, Block base) {
        return register(name, properties -> new StairBlock(base.defaultBlockState(), stone(properties)));
    }

    private static Block stoneSlab(String name) {
        return register(name, properties -> new SlabBlock(stone(properties)));
    }

    private static Block stoneWall(String name) {
        return register(name, properties -> new WallBlock(stone(properties)));
    }

    private static Block stoneFence(String name) {
        return register(name, properties -> new FenceBlock(stone(properties)));
    }

    private static Block stoneFenceGate(String name) {
        return register(
                name,
                properties ->
                        new FenceGateBlock(WoodType.ACACIA, stone(properties).forceSolidOn()));
    }

    private static Block stonePressurePlate(String name) {
        return register(
                name,
                properties -> new PressurePlateBlock(
                        BlockSetType.STONE,
                        properties
                                .strength(0.5f)
                                .requiresCorrectToolForDrops()
                                .sound(SoundType.STONE)
                                .forceSolidOn()
                                .noCollision()
                                .pushReaction(PushReaction.POPPED)));
    }

    public static void init() {}
}

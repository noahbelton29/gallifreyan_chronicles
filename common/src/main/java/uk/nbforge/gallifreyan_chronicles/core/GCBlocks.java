package uk.nbforge.gallifreyan_chronicles.core;

import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.core.block.*;
import uk.nbforge.gallifreyan_chronicles.core.item.ConsoleBlockItem;
import uk.nbforge.gallifreyan_chronicles.core.item.DoorBlockItem;
import uk.nbforge.gallifreyan_chronicles.core.item.InteriorDoorBlockItem;
import uk.nbforge.gallifreyan_chronicles.core.item.TardisBlockItem;
import uk.nbforge.gallifreyan_chronicles.tardis.door.DoorBlockJson;
import uk.nbforge.gallifreyan_chronicles.util.GCUtil;
import uk.nbforge.gallifreyan_chronicles.core.world.tree.GCTreeGrowers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.sounds.AmbientLeavesBlockSoundPlayer;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class GCBlocks {
    public static final List<Block> BLOCKS = new ArrayList<>();
    private static final List<Block> PICKABLE_DOORS = new ArrayList<>();

    private static boolean neverViewBlocking(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            AABB box
    ) {
        return false;
    }

    public static final Block TARDIS = registerBlock("tardis",
            properties -> new TardisBlock(properties.strength(-1F, 3600000.0F)
                    .sound(SoundType.STONE)
                    .noOcclusion()
                    .lightLevel(state -> state.getValue(TardisBlock.LIGHT))),
            Rarity.EPIC,
            TardisBlockItem::new);

    public static final Block TARDIS_TOP = registerBlockWithoutBlockItem("tardis_top",
            properties -> new TardisTopBlock(properties.strength(-1F, 3600000.0F)
                    .sound(SoundType.STONE)
                    .noOcclusion()
                    .noLootTable()));

    public static final Block INTERIOR_DOOR_BARRIER = registerBlockWithoutBlockItem("interior_door_barrier",
            properties -> new InteriorDoorBarrierBlock(properties.strength(-1F, -1F)
                    .noOcclusion()
                    .noLootTable()
                    .isSuffocating(GCBlocks::never)
                    .isViewBlocking(GCBlocks::neverViewBlocking)
                    .pushReaction(PushReaction.IMMOVEABLE)));

    public static final Block TARDIS_CONSOLE = registerBlock("tardis_console",
            properties -> new ConsoleBlock(properties.strength(-1F, -1F)
                    .sound(SoundType.STONE)
                    .noOcclusion()),
            Rarity.EPIC,
            ConsoleBlockItem::new);

    public static final Block TARDIS_INTERIOR_DOOR = registerBlock("tardis_interior_door",
            properties -> new InteriorDoorBlock("first_doctors_interior_doors", properties.strength(-1F, 360000.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
                    .noLootTable()
                    .pushReaction(PushReaction.POPPED)),
            Rarity.COMMON,
            InteriorDoorBlockItem::new);

    static {
        for (DoorBlockJson.Entry door : DoorBlockJson.entries()) {
            registerBlock(door.block(),
                    properties -> new InteriorDoorBlock(door.model(), door.variant(), properties.strength(3F, 3F)
                            .sound(SoundType.STONE)
                            .noOcclusion()
                            .noLootTable()
                            .pushReaction(PushReaction.POPPED)),
                    Rarity.COMMON,
                    DoorBlockItem::new);
        }
    }

    public static final Block GALLIFREYAN_SAND = registerBlock("gallifreyan_sand",
            properties -> new GCFallingBlock(properties.strength(1.25F)
                    .sound(SoundType.SAND)));

    public static final Block MOON_DUST = registerBlock("moon_dust",
            properties -> new GCFallingBlock(properties.strength(1.25F)
                    .sound(SoundType.SAND)));

    public static final Block LUNAR_REGOLITH = registerBlock("lunar_regolith",
            properties -> new Block(properties.strength(3.5F, 3.5F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.STONE)));

    public static final Block MOONSLATE = registerBlock("moonslate",
            properties -> new Block(properties.strength(5.5F, 5.5F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.DEEPSLATE)));

    public static final Block BRACHACKI_FLOOR_BLOCK = registerBlock("brachacki_floor_block",
            properties -> new Block(stone(properties)));

    public static final Block BRACHACKI_WALL_BLOCK = registerBlock("brachacki_wall_block",
            properties -> new Block(stone(properties)));

    public static final Block BRACHACKI_ROUNDEL_BLOCK = registerBlock("brachacki_roundel_block",
            properties -> new Block(stone(properties)));

    public static final Block BRACHACKI_HALF_ROUNDEL_BLOCK = registerBlock("brachacki_half_roundel_block",
            properties -> new Block(stone(properties)));

    public static final Block BRACHACKI_ROUNDEL_ALT_BLOCK = registerBlock("brachacki_roundel_alt_block",
            properties -> new Block(stone(properties)));

    public static final Block BRACHACKI_HALF_ROUNDEL_ALT_BLOCK = registerBlock("brachacki_half_roundel_alt_block",
            properties -> new Block(stone(properties)));

    public static final Block BRACHACKI_WALL_BLOCK_2 = registerBlock("brachacki_wall_block_2",
            properties -> new Block(stone(properties)));

    public static final Block BRACHACKI_ROUNDEL_BLOCK_2 = registerBlock("brachacki_roundel_block_2",
            properties -> new Block(stone(properties)));

    public static final Block BRACHACKI_HALF_ROUNDEL_BLOCK_2 = registerBlock("brachacki_half_roundel_block_2",
            properties -> new Block(stone(properties)));

    public static final Block BRACHACKI_WALL_BLOCK_3 = registerBlock("brachacki_wall_block_3",
            properties -> new Block(stone(properties)));

    public static final Block BRACHACKI_WALL_BLOCK_4 = registerBlock("brachacki_wall_block_4",
            properties -> new Block(stone(properties)));

    public static final Block BRACHACKI_ROUNDEL_BLOCK_3 = registerBlock("brachacki_roundel_block_3",
            properties -> new Block(stone(properties)));

    public static final Block BRACHACKI_ROUNDEL_BLOCK_4 = registerBlock("brachacki_roundel_block_4",
            properties -> new Block(stone(properties)));

    public static final Block BRACHACKI_HALF_ROUNDEL_BLOCK_3 = registerBlock("brachacki_half_roundel_block_3",
            properties -> new Block(stone(properties)));

    public static final Block BRACHACKI_HALF_ROUNDEL_BLOCK_4 = registerBlock("brachacki_half_roundel_block_4",
            properties -> new Block(stone(properties)));

    public static final Block BRACHACKI_LIGHT_STRIP_BLOCK = registerBlock("brachacki_light_strip_block",
            properties -> new Block(stone(properties)));

    public static final Block VOID_BLOCK = registerBlock("void_block",
            properties -> new Block(properties.mapColor(MapColor.COLOR_BLACK)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .strength(1.8F)
                    .sound(SoundType.STONE)
                    .noCollision()
                    .isSuffocating(GCBlocks::never)
                    .isViewBlocking(GCBlocks::neverViewBlocking)));

    public static final Block BRACHACKI_TUAT_FLOOR_BLOCK = registerBlock("brachacki_tuat_floor_block",
            properties -> new Block(stone(properties)));

    public static final Block BRACHACKI_TUAT_WALL_BLOCK = registerBlock("brachacki_tuat_wall_block",
            properties -> new Block(stone(properties)));

    public static final Block BRACHACKI_TUAT_ROUNDEL_BLOCK = registerBlock("brachacki_tuat_roundel_block",
            properties -> new Block(stone(properties)));

    public static final Block BRACHACKI_TUAT_HALF_ROUNDEL_BLOCK = registerBlock("brachacki_tuat_half_roundel_block",
            properties -> new Block(stone(properties)));

    public static final Block BRACHACKI_TUAT_HEXAGONAL_ROUNDEL_BLOCK = registerBlock("brachacki_tuat_hexagonal_roundel_block",
            properties -> new Block(stone(properties)));

    public static final Block BRACHACKI_TUAT_HALF_HEXAGONAL_ROUNDEL_BLOCK = registerBlock("brachacki_tuat_half_hexagonal_roundel_block",
            properties -> new Block(stone(properties)));

    public static final Block BRACHACKI_STAIRS = registerBlock("brachacki_stairs",
            properties -> new StairBlock(BRACHACKI_FLOOR_BLOCK.defaultBlockState(),
                    stone(properties)));

    public static final Block BRACHACKI_SLAB = registerBlock("brachacki_slab",
            properties -> new SlabBlock(stone(properties)));

    public static final Block BRACHACKI_PRESSURE_PLATE = registerBlock("brachacki_pressure_plate",
            properties -> new PressurePlateBlock(BlockSetType.STONE,
                    stonePlate(properties)));

    public static final Block BRACHACKI_WALL = registerBlock("brachacki_wall",
            properties -> new WallBlock(stone(properties)));

    public static final Block BRACHACKI_FENCE = registerBlock("brachacki_fence",
            properties -> new FenceBlock(stone(properties)));

    public static final Block BRACHACKI_FENCE_GATE = registerBlock("brachacki_fence_gate",
            properties -> new FenceGateBlock(WoodType.ACACIA,
                    stone(properties).forceSolidOn()));

    public static final Block BRACHACKI_STAIRS_2 = registerBlock("brachacki_stairs_2",
            properties -> new StairBlock(BRACHACKI_FLOOR_BLOCK.defaultBlockState(),
                    stone(properties)));

    public static final Block BRACHACKI_SLAB_2 = registerBlock("brachacki_slab_2",
            properties -> new SlabBlock(stone(properties)));

    public static final Block BRACHACKI_PRESSURE_PLATE_2 = registerBlock("brachacki_pressure_plate_2",
            properties -> new PressurePlateBlock(BlockSetType.STONE,
                    stonePlate(properties)));

    public static final Block BRACHACKI_WALL_2 = registerBlock("brachacki_wall_2",
            properties -> new WallBlock(stone(properties)));

    public static final Block BRACHACKI_FENCE_2 = registerBlock("brachacki_fence_2",
            properties -> new FenceBlock(stone(properties)));

    public static final Block BRACHACKI_FENCE_GATE_2 = registerBlock("brachacki_fence_gate_2",
            properties -> new FenceGateBlock(WoodType.ACACIA,
                    stone(properties).forceSolidOn()));

    public static final Block BRACHACKI_STAIRS_3 = registerBlock("brachacki_stairs_3",
            properties -> new StairBlock(BRACHACKI_WALL_BLOCK_3.defaultBlockState(),
                    stone(properties)));

    public static final Block BRACHACKI_STAIRS_4 = registerBlock("brachacki_stairs_4",
            properties -> new StairBlock(BRACHACKI_WALL_BLOCK_4.defaultBlockState(),
                    stone(properties)));

    public static final Block BRACHACKI_SLAB_3 = registerBlock("brachacki_slab_3",
            properties -> new SlabBlock(stone(properties)));

    public static final Block BRACHACKI_SLAB_4 = registerBlock("brachacki_slab_4",
            properties -> new SlabBlock(stone(properties)));

    public static final Block BRACHACKI_PRESSURE_PLATE_3 = registerBlock("brachacki_pressure_plate_3",
            properties -> new PressurePlateBlock(BlockSetType.STONE,
                    stonePlate(properties)));

    public static final Block BRACHACKI_PRESSURE_PLATE_4 = registerBlock("brachacki_pressure_plate_4",
            properties -> new PressurePlateBlock(BlockSetType.STONE,
                    stonePlate(properties)));

    public static final Block BRACHACKI_WALL_3 = registerBlock("brachacki_wall_3",
            properties -> new WallBlock(stone(properties)));

    public static final Block BRACHACKI_WALL_4 = registerBlock("brachacki_wall_4",
            properties -> new WallBlock(stone(properties)));

    public static final Block BRACHACKI_FENCE_3 = registerBlock("brachacki_fence_3",
            properties -> new FenceBlock(stone(properties)));

    public static final Block BRACHACKI_FENCE_4 = registerBlock("brachacki_fence_4",
            properties -> new FenceBlock(stone(properties)));

    public static final Block BRACHACKI_FENCE_GATE_3 = registerBlock("brachacki_fence_gate_3",
            properties -> new FenceGateBlock(WoodType.ACACIA,
                    stone(properties).forceSolidOn()));

    public static final Block BRACHACKI_FENCE_GATE_4 = registerBlock("brachacki_fence_gate_4",
            properties -> new FenceGateBlock(WoodType.ACACIA,
                    stone(properties).forceSolidOn()));

    public static final Block BRACHACKI_TUAT_STAIRS = registerBlock("brachacki_tuat_stairs",
            properties -> new StairBlock(BRACHACKI_TUAT_WALL_BLOCK.defaultBlockState(),
                    stone(properties)));

    public static final Block BRACHACKI_TUAT_SLAB = registerBlock("brachacki_tuat_slab",
            properties -> new SlabBlock(stone(properties)));

    public static final Block BRACHACKI_TUAT_PRESSURE_PLATE = registerBlock("brachacki_tuat_pressure_plate",
            properties -> new PressurePlateBlock(BlockSetType.STONE,
                    stonePlate(properties)));

    public static final Block BRACHACKI_TUAT_WALL = registerBlock("brachacki_tuat_wall",
            properties -> new WallBlock(stone(properties)));

    public static final Block BRACHACKI_TUAT_FENCE = registerBlock("brachacki_tuat_fence",
            properties -> new FenceBlock(stone(properties)));

    public static final Block BRACHACKI_TUAT_FENCE_GATE = registerBlock("brachacki_tuat_fence_gate",
            properties -> new FenceGateBlock(WoodType.ACACIA,
                    stone(properties).forceSolidOn()));

    public static final Block GALLIFREYAN_VINE = registerBlock("gallifreyan_vine",
            properties -> new VineBlock(properties.mapColor(MapColor.PLANT)
                    .replaceable()
                    .noCollision()
                    .randomTicks()
                    .strength(0.2F)
                    .sound(SoundType.VINE)
                    .ignitedByLava()
                    .pushReaction(PushReaction.POPPED)));

    public static final Block GALLIFREYAN_GRAVEL = registerBlock("gallifreyan_gravel",
            properties -> new GCFallingBlock(properties.strength(1.25F)
                    .sound(SoundType.GRAVEL)));

    public static final Block GALLIFREYAN_DIRT = registerBlock("gallifreyan_dirt",
            properties -> new Block(properties.strength(0.6F)
                    .sound(SoundType.GRAVEL)));

    public static final Block THORIVINE = registerBlock("thorivine",
            properties -> new ThorivineBlock(properties.mapColor(MapColor.PLANT)
                    .randomTicks()
                    .strength(0.8F)
                    .sound(SoundType.WOOL)
                    .pushReaction(PushReaction.POPPED)));

    public static final Block THORIVINE_FLOWER = registerBlock("thorivine_flower",
            properties -> new CactusFlowerBlock(properties.mapColor(MapColor.COLOR_PINK)
                    .noCollision()
                    .instabreak()
                    .ignitedByLava()
                    .sound(SoundType.CACTUS_FLOWER)
                    .pushReaction(PushReaction.POPPED)));

    public static final Block GALLIFREYAN_GRASS_BLOCK = registerBlock("gallifreyan_grass_block",
            properties -> new GrassBlock(properties.strength(0.7F)
                    .sound(SoundType.GRASS)));

    public static final Block GALLIFREYAN_STONE = registerBlock("gallifreyan_stone",
            properties -> new Block(properties.strength(4.5F, 8.5F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.STONE)));

    public static final Block COBBLED_LIMESTONE = registerBlock("cobbled_limestone",
            properties -> new Block(properties.strength(2.5F, 2.5F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.DRIPSTONE_BLOCK)));

    public static final Block SHORT_GALLIFREYAN_DRY_GRASS = registerBlock("short_gallifreyan_dry_grass",
            properties -> new ShortDryGrassBlock(properties.mapColor(MapColor.COLOR_ORANGE)
                    .replaceable()
                    .noCollision()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .ignitedByLava()
                    .offsetType(BlockBehaviour.OffsetType.XYZ)
                    .pushReaction(PushReaction.POPPED)));

    public static final Block SHORT_GALLIFREYAN_GRASS = registerBlock("short_gallifreyan_grass",
            properties -> new TallGrassBlock(properties.mapColor(MapColor.COLOR_YELLOW)
                    .replaceable()
                    .noCollision()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .ignitedByLava()
                    .offsetType(BlockBehaviour.OffsetType.XYZ)
                    .pushReaction(PushReaction.POPPED)));

    public static final Block TALL_GALLIFREYAN_GRASS = registerBlock("tall_gallifreyan_grass",
            properties -> new DoublePlantBlock(properties.mapColor(MapColor.COLOR_YELLOW)
                    .replaceable()
                    .noCollision()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .ignitedByLava()
                    .offsetType(BlockBehaviour.OffsetType.XYZ)
                    .pushReaction(PushReaction.POPPED)));

    public static final Block TALL_GALLIFREYAN_DRY_GRASS = registerBlock("tall_gallifreyan_dry_grass",
            properties -> new TallDryGrassBlock(properties.mapColor(MapColor.COLOR_ORANGE)
                    .replaceable()
                    .noCollision()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .ignitedByLava()
                    .offsetType(BlockBehaviour.OffsetType.XYZ)
                    .pushReaction(PushReaction.POPPED)));

    public static final Block GALLIFREYAN_STONE_IRON_ORE = registerBlock("gallifreyan_stone_iron_ore",
            properties -> new DropExperienceBlock(UniformInt.of(0, 2),
                    properties.strength(4.5F, 8.5F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.STONE)));

    public static final Block GALLIFREYAN_STONE_COAL_ORE = registerBlock("gallifreyan_stone_coal_ore",
            properties -> new DropExperienceBlock(UniformInt.of(2, 4),
                    properties.strength(4.5F, 8.5F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.STONE)));

    public static final Block GALLIFREYAN_COBBLESTONE = registerBlock("gallifreyan_cobblestone",
            properties -> new Block(properties.strength(4F, 6.5F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.STONE)));

    public static final Block GALLIFREYAN_MUD = registerBlock("gallifreyan_mud",
            properties -> new Block(properties.strength(0.75F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.ROOTED_DIRT)));

    public static final Block GALLIFREYAN_MUDSTONE = registerBlock("gallifreyan_mudstone",
            properties -> new Block(properties.strength(2.75F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.STONE)));

    public static final Block ARCADIAN_SHALE = registerBlock("arcadian_shale",
            properties -> new Block(properties.strength(6.75F, 11.5F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.DEEPSLATE)));

    public static final Block ARCADIAN_SHALE_TARANIUM_ORE = registerBlock("arcadian_shale_taranium_ore",
            properties -> new DropExperienceBlock(UniformInt.of(7, 11),
                    properties.strength(6.75F, 11.5F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.DEEPSLATE)));

    public static final Block CADONWOOD_LOG = registerBlock("cadonwood_log",
            properties -> new RotatedPillarBlock(properties.mapColor(MapColor.WOOD)
                    .strength(2F, 3F)
                    .sound(SoundType.WOOD)
                    .ignitedByLava()));

    public static final Block CADONWOOD_WOOD = registerBlock("cadonwood_wood",
            properties -> new RotatedPillarBlock(properties.mapColor(MapColor.WOOD)
                    .strength(2F, 3F)
                    .sound(SoundType.WOOD)
                    .ignitedByLava()));

    public static final Block STRIPPED_CADONWOOD_LOG = registerBlock("stripped_cadonwood_log",
            properties -> new RotatedPillarBlock(properties.mapColor(MapColor.WOOD)
                    .strength(2F, 3F)
                    .sound(SoundType.WOOD)
                    .ignitedByLava()));

    public static final Block STRIPPED_CADONWOOD_WOOD = registerBlock("stripped_cadonwood_wood",
            properties -> new RotatedPillarBlock(properties.mapColor(MapColor.WOOD)
                    .strength(2F, 3F)
                    .sound(SoundType.WOOD)
                    .ignitedByLava()));

    public static final Block CADONWOOD_PLANKS = registerBlock("cadonwood_planks",
            properties -> new Block(properties.mapColor(MapColor.WOOD)
                    .strength(2F, 3F)
                    .sound(SoundType.WOOD)
                    .ignitedByLava()));

    public static final Block CADONWOOD_LEAVES = registerBlock("cadonwood_leaves",
            properties -> new UntintedParticleLeavesBlock(
                    0F,
                    ParticleTypes.CHERRY_LEAVES,
                    AmbientLeavesBlockSoundPlayer.noAmbientSound(),
                    properties.mapColor(MapColor.METAL)
                            .strength(0.2F)
                            .randomTicks()
                            .sound(SoundType.GRASS)
                            .noOcclusion()
                            .isValidSpawn(GCBlocks::ocelotOrParrot)
                            .isSuffocating(GCBlocks::never)
                            .isViewBlocking(GCBlocks::neverViewBlocking)
                            .ignitedByLava()
                            .pushReaction(PushReaction.POPPED)
                            .isRedstoneConductor(GCBlocks::never)));

    public static final Block CADONWOOD_STAIRS = registerBlock("cadonwood_stairs",
            properties -> new StairBlock(CADONWOOD_PLANKS.defaultBlockState(),
                    properties.mapColor(MapColor.WOOD)
                            .strength(2F, 3F)
                            .sound(SoundType.WOOD)
                            .ignitedByLava()));

    public static final Block CADONWOOD_SLAB = registerBlock("cadonwood_slab",
            properties -> new SlabBlock(properties.mapColor(MapColor.WOOD)
                    .strength(2F, 3F)
                    .sound(SoundType.WOOD)
                    .ignitedByLava()));

    public static final Block CADONWOOD_PRESSURE_PLATE = registerBlock("cadonwood_pressure_plate",
            properties -> new PressurePlateBlock(BlockSetType.ACACIA,
                    properties.mapColor(MapColor.WOOD)
                            .strength(2F, 3F)
                            .sound(SoundType.WOOD)
                            .ignitedByLava()
                            .forceSolidOn()
                            .noCollision()
                            .pushReaction(PushReaction.POPPED)));

    public static final Block CADONWOOD_BUTTON = registerBlock("cadonwood_button",
            properties -> new ButtonBlock(BlockSetType.ACACIA, 30,
                    properties.mapColor(MapColor.WOOD)
                            .strength(2F, 3F)
                            .sound(SoundType.WOOD)
                            .ignitedByLava()
                            .noCollision()
                            .pushReaction(PushReaction.POPPED)));

    public static final Block CADONWOOD_FENCE = registerBlock("cadonwood_fence",
            properties -> new FenceBlock(properties.mapColor(MapColor.WOOD)
                    .strength(2F, 3F)
                    .sound(SoundType.WOOD)
                    .ignitedByLava()));

    public static final Block CADONWOOD_FENCE_GATE = registerBlock("cadonwood_fence_gate",
            properties -> new FenceGateBlock(WoodType.ACACIA,
                    properties.mapColor(MapColor.WOOD)
                            .strength(2F, 3F)
                            .sound(SoundType.WOOD)
                            .ignitedByLava()));

    public static final Block CADONWOOD_WALL = registerBlock("cadonwood_wall",
            properties -> new WallBlock(properties.mapColor(MapColor.WOOD)
                    .strength(2F, 3F)
                    .sound(SoundType.WOOD)
                    .ignitedByLava()));

    private static boolean never(
            BlockState blockState,
            BlockGetter blockGetter,
            BlockPos blockPos
    ) {
        return false;
    }

    public static final List<String> ROUNDEL_WOODS = List.of(
            "oak", "spruce", "birch", "jungle", "acacia", "dark_oak", "mangrove",
            "cherry", "pale_oak", "bamboo", "crimson", "warped", "cadonwood"
    );
    public static final Map<String, Block> WOOD_ROUNDELS = new LinkedHashMap<>();
    public static final Map<String, Block> WOOD_HALF_ROUNDELS = new LinkedHashMap<>();

    static {
        for (String wood : ROUNDEL_WOODS) {
            WOOD_ROUNDELS.put(wood, registerWoodRoundel(wood + "_roundel_block", wood));
            WOOD_HALF_ROUNDELS.put(wood, registerWoodRoundel(wood + "_half_roundel_block", wood));
        }
    }

    public static Block woodPlanks(String wood) {
        return switch (wood) {
            case "oak" -> Blocks.OAK_PLANKS;
            case "spruce" -> Blocks.SPRUCE_PLANKS;
            case "birch" -> Blocks.BIRCH_PLANKS;
            case "jungle" -> Blocks.JUNGLE_PLANKS;
            case "acacia" -> Blocks.ACACIA_PLANKS;
            case "dark_oak" -> Blocks.DARK_OAK_PLANKS;
            case "mangrove" -> Blocks.MANGROVE_PLANKS;
            case "cherry" -> Blocks.CHERRY_PLANKS;
            case "pale_oak" -> Blocks.PALE_OAK_PLANKS;
            case "bamboo" -> Blocks.BAMBOO_PLANKS;
            case "crimson" -> Blocks.CRIMSON_PLANKS;
            case "warped" -> Blocks.WARPED_PLANKS;
            case "cadonwood" -> CADONWOOD_PLANKS;
            default -> throw new IllegalArgumentException("Unknown roundel wood " + wood);
        };
    }

    private static Block registerWoodRoundel(String name, String wood) {
        Block planks = woodPlanks(wood);
        boolean nether = wood.equals("crimson") || wood.equals("warped");
        SoundType sound = switch (wood) {
            case "cherry" -> SoundType.CHERRY_WOOD;
            case "bamboo" -> SoundType.BAMBOO_WOOD;
            case "crimson", "warped" -> SoundType.NETHER_WOOD;
            default -> SoundType.WOOD;
        };
        return registerBlock(name, properties -> {
            properties.mapColor(planks.defaultMapColor())
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(2F, 3F)
                    .sound(sound);
            if (!nether) properties.ignitedByLava();
            return new Block(properties);
        });
    }

    public static final List<String> ROUNDEL_CONCRETES = List.of(
            "white", "light_gray", "gray", "black", "brown", "red", "orange", "yellow",
            "lime", "green", "cyan", "light_blue", "blue", "purple", "magenta", "pink"
    );
    public static final Map<String, Block> CONCRETE_ROUNDELS = new LinkedHashMap<>();
    public static final Map<String, Block> CONCRETE_HALF_ROUNDELS = new LinkedHashMap<>();

    static {
        for (String colour : ROUNDEL_CONCRETES) {
            CONCRETE_ROUNDELS.put(colour, registerConcreteRoundel(colour + "_concrete_roundel_block", colour));
            CONCRETE_HALF_ROUNDELS.put(colour, registerConcreteRoundel(colour + "_concrete_half_roundel_block", colour));
        }
    }

    public static Block concrete(String colour) {
        return Blocks.CONCRETE.pick(DyeColor.byName(colour, DyeColor.WHITE));
    }

    private static Block registerConcreteRoundel(String name, String colour) {
        DyeColor dye = DyeColor.byName(colour, DyeColor.WHITE);
        return registerBlock(name, properties -> new Block(properties
                .mapColor(dye)
                .instrument(NoteBlockInstrument.BASEDRUM)
                .requiresCorrectToolForDrops()
                .strength(1.8F)));
    }

    public static final Block CADONWOOD_SAPLING = registerBlock("cadonwood_sapling",
            properties -> new SaplingBlock(GCTreeGrowers.CADONWOOD,
                    properties.mapColor(MapColor.GRASS)
                            .noCollision()
                            .randomTicks()
                            .instabreak()
                            .sound(SoundType.GRASS)
                            .pushReaction(PushReaction.POPPED)));

    public static final Block POTTED_CADONWOOD_SAPLING = registerBlockWithoutBlockItem(
            "potted_cadonwood_sapling",
            properties -> new FlowerPotBlock(CADONWOOD_SAPLING,
                    properties.mapColor(MapColor.GRASS)
                            .noOcclusion()
                            .instabreak()
                            .pushReaction(PushReaction.POPPED)));

    private static Boolean ocelotOrParrot(
            BlockState state,
            BlockGetter blockGetter,
            BlockPos blockPos,
            EntityType<?> entityType
    ) {
        return entityType == EntityTypes.OCELOT || entityType == EntityTypes.PARROT;
    }

    private static BlockBehaviour.Properties stone(BlockBehaviour.Properties properties) {
        return properties.strength(3F, 3F).requiresCorrectToolForDrops().sound(SoundType.STONE);
    }

    private static BlockBehaviour.Properties stonePlate(BlockBehaviour.Properties properties) {
        return properties.strength(0.5F).requiresCorrectToolForDrops().sound(SoundType.STONE)
                .forceSolidOn().noCollision().pushReaction(PushReaction.POPPED);
    }

    private static Block registerBlock(
            String name,
            Function<BlockBehaviour.Properties, Block> function,
            Rarity rarity
    ) {
        return registerBlock(name, function, rarity, BlockItem::new);
    }

    private static Block registerBlock(
            String name,
            Function<BlockBehaviour.Properties, Block> function,
            Rarity rarity,
            BiFunction<Block, Item.Properties, Item> itemFactory
    ) {
        Block toRegister = function.apply(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, GCUtil.modIdentifier(name))));

        Item item = registerBlockItem(name, toRegister, rarity, itemFactory);
        BLOCKS.add(toRegister);
        if (item instanceof DoorBlockItem) PICKABLE_DOORS.add(toRegister);

        return Registry.register(BuiltInRegistries.BLOCK, GCUtil.modIdentifier(name), toRegister);
    }

    private static Block registerBlock(
            String name,
            Function<BlockBehaviour.Properties, Block> function
    ) {
        return registerBlock(name, function, Rarity.COMMON);
    }

    private static Block registerBlockWithoutBlockItem(
            String name,
            Function<BlockBehaviour.Properties, Block> function
    ) {
        Block toRegister = function.apply(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, GCUtil.modIdentifier(name))));

        BLOCKS.add(toRegister);

        return Registry.register(BuiltInRegistries.BLOCK, GCUtil.modIdentifier(name), toRegister);
    }

    private static Item registerBlockItem(String name, Block block, Rarity rarity,
                                          BiFunction<Block, Item.Properties, Item> itemFactory) {
        return Registry.register(BuiltInRegistries.ITEM, GCUtil.modIdentifier(name),
                itemFactory.apply(block, new Item.Properties()
                        .useBlockDescriptionPrefix()
                        .rarity(rarity)
                        .setId(ResourceKey.create(Registries.ITEM, GCUtil.modIdentifier(name)))));
    }

    /* every interior door block (the tardis one and the plain ones) */
    public static Block[] interiorDoorBlocks() {
        return BLOCKS.stream().filter(block -> block instanceof InteriorDoorBlock).toArray(Block[]::new);
    }

    /* the plain doors one door item scrolls between, in json priority order */
    public static List<Block> pickableDoors() {
        return List.copyOf(PICKABLE_DOORS);
    }

    public static void registerBlocks() {
        Constants.LOG.info("Registered GC Blocks");
    }
}
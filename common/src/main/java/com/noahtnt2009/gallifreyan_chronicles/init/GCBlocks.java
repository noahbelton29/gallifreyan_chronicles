package com.noahtnt2009.gallifreyan_chronicles.init;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.block.*;
import com.noahtnt2009.gallifreyan_chronicles.item.ConsoleBlockItem;
import com.noahtnt2009.gallifreyan_chronicles.item.TardisBlockItem;
import com.noahtnt2009.gallifreyan_chronicles.util.GCUtils;
import com.noahtnt2009.gallifreyan_chronicles.world.tree.GCTreeGrowers;
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
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class GCBlocks {
    public static final List<Block> BLOCKS = new ArrayList<>();

    private static boolean neverViewBlocking(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            AABB box
    ) {
        return false;
    }

    public static final Block TARDIS = registerBlock("tardis",
            properties -> new TardisBlock(properties.strength(-1F)
                    .sound(SoundType.STONE)
                    .noOcclusion()
                    .lightLevel(state -> state.getValue(TardisBlock.GLOWING) ? 12 : 0)),
            Rarity.EPIC,
            TardisBlockItem::new);

    public static final Block TARDIS_TOP = registerBlockWithoutBlockItem("tardis_top",
            properties -> new TardisTopBlock(properties.strength(-1F)
                    .sound(SoundType.STONE)
                    .noOcclusion()
                    .noLootTable()));

    public static final Block TARDIS_CONSOLE = registerBlock("tardis_console",
            properties -> new ConsoleBlock(properties.strength(-1F)
                    .sound(SoundType.STONE)
                    .noOcclusion()),
            Rarity.EPIC,
            ConsoleBlockItem::new);

    public static final Block TARDIS_INTERIOR_DOOR = registerBlock("tardis_interior_door",
            properties -> new InteriorDoorBlock("brachacki_doors", properties.strength(3F, 3F)
                    .sound(SoundType.STONE)
                    .noOcclusion()
                    .noLootTable()
                    .pushReaction(PushReaction.POPPED)));

    public static final Block FIRST_DOCTORS_DOOR = registerBlock("first_doctors_door",
            properties -> new InteriorDoorBlock("brachacki_doors", "first_doctors_doors", properties.strength(3F, 3F)
                    .sound(SoundType.STONE)
                    .noOcclusion()
                    .noLootTable()
                    .pushReaction(PushReaction.POPPED)));

    public static final Block SECOND_DOCTORS_DOOR = registerBlock("second_doctors_door",
            properties -> new InteriorDoorBlock("brachacki_doors", "second_doctors_doors", properties.strength(3F, 3F)
                    .sound(SoundType.STONE)
                    .noOcclusion()
                    .noLootTable()
                    .pushReaction(PushReaction.POPPED)));

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
            properties -> new Block(properties.strength(3F, 3F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.STONE)));

    public static final Block BRACHACKI_WALL_BLOCK = registerBlock("brachacki_wall_block",
            properties -> new Block(properties.strength(3F, 3F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.STONE)));

    public static final Block BRACHACKI_ROUNDEL_BLOCK = registerBlock("brachacki_roundel_block",
            properties -> new Block(properties.strength(3F, 3F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.STONE)));

    public static final Block BRACHACKI_HALF_ROUNDEL_BLOCK = registerBlock("brachacki_half_roundel_block",
            properties -> new Block(properties.strength(3F, 3F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.STONE)));

    public static final Block BRACHACKI_ROUNDEL_ALT_BLOCK = registerBlock("brachacki_roundel_alt_block",
            properties -> new Block(properties.strength(3F, 3F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.STONE)));

    public static final Block BRACHACKI_HALF_ROUNDEL_ALT_BLOCK = registerBlock("brachacki_half_roundel_alt_block",
            properties -> new Block(properties.strength(3F, 3F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.STONE)));

    public static final Block BRACHACKI_WALL_BLOCK_2 = registerBlock("brachacki_wall_block_2",
            properties -> new Block(properties.strength(3F, 3F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.STONE)));

    public static final Block BRACHACKI_ROUNDEL_BLOCK_2 = registerBlock("brachacki_roundel_block_2",
            properties -> new Block(properties.strength(3F, 3F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.STONE)));

    public static final Block BRACHACKI_HALF_ROUNDEL_BLOCK_2 = registerBlock("brachacki_half_roundel_block_2",
            properties -> new Block(properties.strength(3F, 3F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.STONE)));

    public static final Block BRACHACKI_STAIRS = registerBlock("brachacki_stairs",
            properties -> new StairBlock(BRACHACKI_FLOOR_BLOCK.defaultBlockState(),
                    properties.strength(3F, 3F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.STONE)));

    public static final Block BRACHACKI_SLAB = registerBlock("brachacki_slab",
            properties -> new SlabBlock(properties.strength(3F, 3F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.STONE)));

    public static final Block BRACHACKI_PRESSURE_PLATE = registerBlock("brachacki_pressure_plate",
            properties -> new PressurePlateBlock(BlockSetType.STONE,
                    properties.strength(0.5F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.STONE)
                            .forceSolidOn()
                            .noCollision()
                            .pushReaction(PushReaction.POPPED)));

    public static final Block BRACHACKI_WALL = registerBlock("brachacki_wall",
            properties -> new WallBlock(properties.strength(3F, 3F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.STONE)));

    public static final Block BRACHACKI_FENCE = registerBlock("brachacki_fence",
            properties -> new FenceBlock(properties.strength(3F, 3F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.STONE)));

    public static final Block BRACHACKI_FENCE_GATE = registerBlock("brachacki_fence_gate",
            properties -> new FenceGateBlock(WoodType.ACACIA,
                    properties.strength(3F, 3F)
                            .forceSolidOn()
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.STONE)));

    public static final Block BRACHACKI_STAIRS_2 = registerBlock("brachacki_stairs_2",
            properties -> new StairBlock(BRACHACKI_FLOOR_BLOCK.defaultBlockState(),
                    properties.strength(3F, 3F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.STONE)));

    public static final Block BRACHACKI_SLAB_2 = registerBlock("brachacki_slab_2",
            properties -> new SlabBlock(properties.strength(3F, 3F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.STONE)));

    public static final Block BRACHACKI_PRESSURE_PLATE_2 = registerBlock("brachacki_pressure_plate_2",
            properties -> new PressurePlateBlock(BlockSetType.STONE,
                    properties.strength(0.5F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.STONE)
                            .forceSolidOn()
                            .noCollision()
                            .pushReaction(PushReaction.POPPED)));

    public static final Block BRACHACKI_WALL_2 = registerBlock("brachacki_wall_2",
            properties -> new WallBlock(properties.strength(3F, 3F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.STONE)));

    public static final Block BRACHACKI_FENCE_2 = registerBlock("brachacki_fence_2",
            properties -> new FenceBlock(properties.strength(3F, 3F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.STONE)));

    public static final Block BRACHACKI_FENCE_GATE_2 = registerBlock("brachacki_fence_gate_2",
            properties -> new FenceGateBlock(WoodType.ACACIA,
                    properties.strength(3F, 3F)
                            .forceSolidOn()
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.STONE)));

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
                .setId(ResourceKey.create(Registries.BLOCK, GCUtils.modIdentifier(name))));

        registerBlockItem(name, toRegister, rarity, itemFactory);
        BLOCKS.add(toRegister);

        return Registry.register(BuiltInRegistries.BLOCK, GCUtils.modIdentifier(name), toRegister);
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
                .setId(ResourceKey.create(Registries.BLOCK, GCUtils.modIdentifier(name))));

        BLOCKS.add(toRegister);

        return Registry.register(BuiltInRegistries.BLOCK, GCUtils.modIdentifier(name), toRegister);
    }

    private static void registerBlockItem(String name, Block block, Rarity rarity,
                                          BiFunction<Block, Item.Properties, Item> itemFactory) {
        Registry.register(BuiltInRegistries.ITEM, GCUtils.modIdentifier(name),
                itemFactory.apply(block, new Item.Properties()
                        .useBlockDescriptionPrefix()
                        .rarity(rarity)
                        .setId(ResourceKey.create(Registries.ITEM, GCUtils.modIdentifier(name)))));
    }

    public static void registerBlocks() {
        Constants.LOG.info("Registered GC Blocks");
    }
}

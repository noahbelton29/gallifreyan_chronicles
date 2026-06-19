package com.noahtnt2009.gallifreyan_chronicles.registry;

import com.mojang.serialization.MapCodec;
import com.noahtnt2009.gallifreyan_chronicles.GallifreyanChronicles;
import com.noahtnt2009.gallifreyan_chronicles.block.GCSaplingBlock;
import com.noahtnt2009.gallifreyan_chronicles.block.GallifreyanGravelBlock;
import com.noahtnt2009.gallifreyan_chronicles.block.GallifreyanSandBlock;
import com.noahtnt2009.gallifreyan_chronicles.block.TardisBlock;
import com.noahtnt2009.gallifreyan_chronicles.world.tree.GCTreeGrowers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class GCBlocks {
    public static final List<Block> BLOCKS = new ArrayList<>();

    public static final Block GALLIFREYAN_SAND = registerBlock("gallifreyan_sand",
            properties -> new GallifreyanSandBlock(properties.strength(1.25f)
                    .sound(SoundType.SAND)));

    public static final Block TARDIS_BLOCK = registerBlock("tardis_block",
            properties -> new TardisBlock(properties.strength(-1F)
                    .sound(SoundType.STONE).noOcclusion()));

    public static final Block GALLIFREYAN_GRAVEL = registerBlock("gallifreyan_gravel",
            properties -> new GallifreyanGravelBlock(properties.strength(1.25f)
                    .sound(SoundType.GRAVEL)));

    public static final Block GALLIFREYAN_DIRT = registerBlock("gallifreyan_dirt",
            properties -> new Block(properties.strength(0.6F)
                    .sound(SoundType.GRAVEL)));

    public static final Block GALLIFREYAN_GRASS_BLOCK = registerBlock("gallifreyan_grass_block",
            properties -> new GrassBlock(properties.strength(0.7F)
                    .sound(SoundType.GRASS)));

    public static final Block GALLIFREYAN_STONE = registerBlock("gallifreyan_stone",
            properties -> new Block(properties.strength(4.5f, 8.5f)
                    .requiresCorrectToolForDrops().sound(SoundType.STONE)));

    public static final Block SHORT_GALLIFREYAN_DRY_GRASS = registerBlock("short_gallifreyan_dry_grass",
            properties -> new ShortDryGrassBlock(properties.mapColor(MapColor.COLOR_ORANGE).replaceable()
                    .noCollision().instabreak().sound(SoundType.GRASS).ignitedByLava().offsetType(BlockBehaviour.OffsetType.XYZ)
                    .pushReaction(PushReaction.DESTROY)));

    public static final Block SHORT_GALLIFREYAN_GRASS = registerBlock("short_gallifreyan_grass",
            properties -> new TallGrassBlock(properties.mapColor(MapColor.COLOR_YELLOW).replaceable()
                    .noCollision().instabreak().sound(SoundType.GRASS).ignitedByLava().offsetType(BlockBehaviour.OffsetType.XYZ)
                    .pushReaction(PushReaction.DESTROY)));

    public static final Block TALL_GALLIFREYAN_GRASS = registerBlock("tall_gallifreyan_grass",
            properties -> new DoublePlantBlock(properties.mapColor(MapColor.COLOR_YELLOW).replaceable()
                    .noCollision().instabreak().sound(SoundType.GRASS).ignitedByLava().offsetType(BlockBehaviour.OffsetType.XYZ)
                    .pushReaction(PushReaction.DESTROY)));

    public static final Block TALL_GALLIFREYAN_DRY_GRASS = registerBlock("tall_gallifreyan_dry_grass",
            properties -> new TallDryGrassBlock(properties.mapColor(MapColor.COLOR_ORANGE).replaceable()
                    .noCollision().instabreak().sound(SoundType.GRASS).ignitedByLava().offsetType(BlockBehaviour.OffsetType.XYZ)
                    .pushReaction(PushReaction.DESTROY)));

    public static final Block GALLIFREYAN_STONE_IRON_ORE = registerBlock("gallifreyan_stone_iron_ore",
            properties -> new DropExperienceBlock(UniformInt.of(0, 2), properties.strength(4.5f, 8.5f)
                    .requiresCorrectToolForDrops().sound(SoundType.STONE)));

    public static final Block GALLIFREYAN_STONE_COAL_ORE = registerBlock("gallifreyan_stone_coal_ore",
            properties -> new DropExperienceBlock(UniformInt.of(2, 4), properties.strength(4.5f, 8.5f)
                    .requiresCorrectToolForDrops().sound(SoundType.STONE)));

    public static final Block GALLIFREYAN_COBBLESTONE = registerBlock("gallifreyan_cobblestone",
            properties -> new Block(properties.strength(4.0f, 6.5f)
                    .requiresCorrectToolForDrops().sound(SoundType.STONE)));

    public static final Block GALLIFREYAN_MUD = registerBlock("gallifreyan_mud",
            properties -> new Block(properties.strength(0.75f)
                    .requiresCorrectToolForDrops().sound(SoundType.ROOTED_DIRT)));

    public static final Block ARCADIAN_SHALE = registerBlock("arcadian_shale",
            properties -> new Block(properties.strength(6.75f, 11.5f)
                    .requiresCorrectToolForDrops().sound(SoundType.DEEPSLATE)));

    public static final Block CADONWOOD_LOG = registerBlock("cadonwood_log",
            properties -> new RotatedPillarBlock(properties.mapColor(MapColor.WOOD).strength(2F, 3F)
                    .sound(SoundType.WOOD).ignitedByLava()));

    public static final Block CADONWOOD_WOOD = registerBlock("cadonwood_wood",
            properties -> new RotatedPillarBlock(properties.mapColor(MapColor.WOOD).strength(2F, 3F)
                    .sound(SoundType.WOOD).ignitedByLava()));

    public static final Block STRIPPED_CADONWOOD_LOG = registerBlock("stripped_cadonwood_log",
            properties -> new RotatedPillarBlock(properties.mapColor(MapColor.WOOD).strength(2F, 3F)
                    .sound(SoundType.WOOD).ignitedByLava()));

    public static final Block STRIPPED_CADONWOOD_WOOD = registerBlock("stripped_cadonwood_wood",
            properties -> new RotatedPillarBlock(properties.mapColor(MapColor.WOOD).strength(2F, 3F)
                    .sound(SoundType.WOOD).ignitedByLava()));

    public static final Block CADONWOOD_PLANKS = registerBlock("cadonwood_planks",
            properties -> new Block(properties.mapColor(MapColor.WOOD).strength(2F, 3F)
                    .sound(SoundType.WOOD).ignitedByLava()));

    public static final Block CADONWOOD_LEAVES = registerBlock("cadonwood_leaves",
            properties -> new UntintedParticleLeavesBlock(0f, ParticleTypes.CHERRY_LEAVES,
                    properties.mapColor(MapColor.METAL).strength(0.2F).randomTicks().sound(SoundType.GRASS)
                            .noOcclusion().isValidSpawn(Blocks::ocelotOrParrot).isSuffocating(Blocks::never)
                            .isViewBlocking(Blocks::never).ignitedByLava().pushReaction(PushReaction.DESTROY)
                            .isRedstoneConductor(Blocks::never)));

    public static final Block CADONWOOD_SAPLING = registerBlock("cadonwood_sapling",
            properties -> new SaplingBlock(GCTreeGrowers.CADONWOOD, properties.mapColor(MapColor.GRASS).noCollision()
                    .randomTicks().instabreak().sound(SoundType.GRASS).pushReaction(PushReaction.DESTROY)));

    public static final Block POTTED_CADONWOOD_SAPLING = registerBlockWithoutBlockItem("potted_cadonwood_sapling",
            properties -> new FlowerPotBlock(CADONWOOD_SAPLING, properties.mapColor(MapColor.GRASS).noOcclusion()
                    .instabreak().pushReaction(PushReaction.DESTROY)));

    private static Block registerBlockWithoutBlockItem(String name, Function<BlockBehaviour.Properties, Block> function) {
        return Registry.register(BuiltInRegistries.BLOCK, Identifier
                .fromNamespaceAndPath(GallifreyanChronicles.MOD_ID, name), function.apply(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, Identifier
                        .fromNamespaceAndPath(GallifreyanChronicles.MOD_ID, name)))));
    }

    private static Block registerBlock(String name, Function<BlockBehaviour.Properties, Block> function) {
        Block toRegister = function.apply(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, Identifier
                        .fromNamespaceAndPath(GallifreyanChronicles.MOD_ID, name))));
        registerBlockItem(name, toRegister);
        BLOCKS.add(toRegister);
        return Registry.register(BuiltInRegistries.BLOCK, Identifier
                .fromNamespaceAndPath(GallifreyanChronicles.MOD_ID, name), toRegister);
    }

    private static void registerBlockItem(String name, Block block) {
        Registry.register(BuiltInRegistries.ITEM, Identifier.fromNamespaceAndPath(GallifreyanChronicles.MOD_ID, name),
                new BlockItem(block, new Item.Properties().useBlockDescriptionPrefix()
                        .setId(ResourceKey.create(Registries.ITEM, Identifier
                                .fromNamespaceAndPath(GallifreyanChronicles.MOD_ID, name)))));
    }

    public static void init() {
        GallifreyanChronicles.LOGGER.info("Registering Mod Blocks for " + GallifreyanChronicles.MOD_ID);
    }
}

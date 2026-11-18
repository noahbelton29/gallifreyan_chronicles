package uk.nbforge.gallifreyan_chronicles.core.block.registry;

import static uk.nbforge.gallifreyan_chronicles.core.GCBlocks.register;
import static uk.nbforge.gallifreyan_chronicles.core.GCBlocks.registerWithoutItem;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.UntintedParticleLeavesBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.sounds.AmbientLeavesBlockSoundPlayer;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import uk.nbforge.gallifreyan_chronicles.core.GCBlocks;
import uk.nbforge.gallifreyan_chronicles.core.world.tree.GCTreeGrowers;

public final class CadonwoodBlocks {
    private CadonwoodBlocks() {}

    public static final Block CADONWOOD_LOG =
            register("cadonwood_log", properties -> new RotatedPillarBlock(wood(properties)));
    public static final Block CADONWOOD_WOOD =
            register("cadonwood_wood", properties -> new RotatedPillarBlock(wood(properties)));
    public static final Block STRIPPED_CADONWOOD_LOG =
            register("stripped_cadonwood_log", properties -> new RotatedPillarBlock(wood(properties)));
    public static final Block STRIPPED_CADONWOOD_WOOD =
            register("stripped_cadonwood_wood", properties -> new RotatedPillarBlock(wood(properties)));
    public static final Block CADONWOOD_PLANKS =
            register("cadonwood_planks", properties -> new Block(wood(properties)));

    public static final Block CADONWOOD_LEAVES = register(
            "cadonwood_leaves",
            properties -> new UntintedParticleLeavesBlock(
                    0f,
                    ParticleTypes.CHERRY_LEAVES,
                    AmbientLeavesBlockSoundPlayer.noAmbientSound(),
                    properties
                            .mapColor(MapColor.METAL)
                            .strength(0.2f)
                            .randomTicks()
                            .sound(SoundType.GRASS)
                            .noOcclusion()
                            .isValidSpawn(CadonwoodBlocks::ocelotOrParrot)
                            .isSuffocating(GCBlocks::never)
                            .isViewBlocking(GCBlocks::neverViewBlocking)
                            .ignitedByLava()
                            .pushReaction(PushReaction.POPPED)
                            .isRedstoneConductor(GCBlocks::never)));

    public static final Block CADONWOOD_STAIRS = register(
            "cadonwood_stairs", properties -> new StairBlock(CADONWOOD_PLANKS.defaultBlockState(), wood(properties)));
    public static final Block CADONWOOD_SLAB =
            register("cadonwood_slab", properties -> new SlabBlock(wood(properties)));
    public static final Block CADONWOOD_PRESSURE_PLATE = register(
            "cadonwood_pressure_plate",
            properties -> new PressurePlateBlock(
                    BlockSetType.ACACIA,
                    wood(properties).forceSolidOn().noCollision().pushReaction(PushReaction.POPPED)));
    public static final Block CADONWOOD_BUTTON = register(
            "cadonwood_button",
            properties -> new ButtonBlock(
                    BlockSetType.ACACIA, 30, wood(properties).noCollision().pushReaction(PushReaction.POPPED)));
    public static final Block CADONWOOD_FENCE =
            register("cadonwood_fence", properties -> new FenceBlock(wood(properties)));
    public static final Block CADONWOOD_FENCE_GATE =
            register("cadonwood_fence_gate", properties -> new FenceGateBlock(WoodType.ACACIA, wood(properties)));
    public static final Block CADONWOOD_WALL =
            register("cadonwood_wall", properties -> new WallBlock(wood(properties)));

    public static final Block CADONWOOD_SAPLING = register(
            "cadonwood_sapling",
            properties -> new SaplingBlock(
                    GCTreeGrowers.CADONWOOD,
                    properties
                            .mapColor(MapColor.GRASS)
                            .noCollision()
                            .randomTicks()
                            .instabreak()
                            .sound(SoundType.GRASS)
                            .pushReaction(PushReaction.POPPED)));

    public static final Block POTTED_CADONWOOD_SAPLING = registerWithoutItem(
            "potted_cadonwood_sapling",
            properties -> new FlowerPotBlock(
                    CADONWOOD_SAPLING,
                    properties
                            .mapColor(MapColor.GRASS)
                            .noOcclusion()
                            .instabreak()
                            .pushReaction(PushReaction.POPPED)));

    private static BlockBehaviour.Properties wood(BlockBehaviour.Properties properties) {
        return properties
                .mapColor(MapColor.WOOD)
                .strength(2f, 3f)
                .sound(SoundType.WOOD)
                .ignitedByLava();
    }

    private static Boolean ocelotOrParrot(
            BlockState state, BlockGetter blockGetter, BlockPos blockPos, EntityType<?> entityType) {
        return entityType == EntityTypes.OCELOT || entityType == EntityTypes.PARROT;
    }

    public static void init() {}
}

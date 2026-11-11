package com.noahtnt2009.gallifreyan_chronicles.tardis.flight;

import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisExteriorBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.init.GCBlocks;
import com.noahtnt2009.gallifreyan_chronicles.tardis.api.Tardis;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.system.TardisLandingSystem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class TardisCrater {
    private static final int RADIUS = 12;
    private static final int DEPTH = 6;
    private static final int CLEAR_ABOVE = 4;
    private static final int RIM = 2;
    private static final float FIRE_CHANCE = 0.4F;
    private static final float LASTING_FIRE = 0.5F;

    private TardisCrater() {
    }

    public static void blast(Tardis tardis) {
        TardisExteriorBlockEntity exterior = tardis.exteriorIfLoaded().orElse(null);
        if (exterior == null || !(exterior.getLevel() instanceof ServerLevel level)) return;
        BlockPos ground = exterior.getBlockPos().below();
        RandomSource random = level.getRandom();

        carveBowl(level, ground, random);
        throwUpRim(level, ground, random);
        blastEffects(level, ground);
        dropTardisIn(tardis, exterior, level, ground);
    }

    private static void carveBowl(ServerLevel level, BlockPos ground, RandomSource random) {
        for (int dx = -RADIUS; dx <= RADIUS; dx++) {
            for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                double distanceFromCentre = (dx * dx + dz * dz) / (double) (RADIUS * RADIUS);
                if (distanceFromCentre > 1.0) continue;

                int bottom = -depthAt(distanceFromCentre, random);
                for (int dy = bottom; dy <= CLEAR_ABOVE; dy++) {
                    clear(level, ground.offset(dx, dy, dz));
                }
                scorch(level, ground.offset(dx, bottom - 1, dz), random);
            }
        }
    }

    private static int depthAt(double distanceFromCentre, RandomSource random) {
        double roughness = 0.85 + random.nextDouble() * 0.3;
        return (int) Math.round(DEPTH * Math.sqrt(1.0 - distanceFromCentre) * roughness);
    }

    private static void blastEffects(ServerLevel level, BlockPos ground) {
        double x = ground.getX() + 0.5;
        double z = ground.getZ() + 0.5;
        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, x, ground.getY() + 1, z,
                6, RADIUS * 0.3, 1.0, RADIUS * 0.3, 0);
        level.sendParticles(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, x, ground.getY(), z,
                30, RADIUS * 0.4, 1.0, RADIUS * 0.4, 0.02);
        level.playSound(null, ground, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 8.0F, 0.5F);
    }

    private static void dropTardisIn(Tardis tardis, TardisExteriorBlockEntity exterior, ServerLevel level, BlockPos ground) {
        BlockPos floor = TardisLandingSystem.findLandingSpot(level, ground.below(DEPTH));
        if (!floor.equals(exterior.getBlockPos())) {
            TardisLandingSystem.moveExterior(tardis, level, floor);
        }
    }

    private static boolean solid(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return !state.isAir() && state.getFluidState().isEmpty() && !state.canBeReplaced();
    }

    private static boolean breakable(ServerLevel level, BlockPos pos, BlockState state) {
        return !state.isAir() && state.getDestroySpeed(level, pos) >= 0 && level.getBlockEntity(pos) == null
                && !state.is(GCBlocks.TARDIS) && !state.is(GCBlocks.TARDIS_TOP);
    }

    private static void clear(ServerLevel level, BlockPos pos) {
        if (level.isOutsideBuildHeight(pos)) return;
        BlockState state = level.getBlockState(pos);
        if (!breakable(level, pos, state)) return;
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_SUPPRESS_DROPS);
    }

    private static void scorch(ServerLevel level, BlockPos floor, RandomSource random) {
        if (level.isOutsideBuildHeight(floor)) return;
        if (!breakable(level, floor, level.getBlockState(floor))) return;

        boolean onFire = random.nextFloat() < FIRE_CHANCE && level.getBlockState(floor.above()).isAir();
        if (onFire) {
            level.setBlock(floor, randomFireBase(random), Block.UPDATE_CLIENTS);
            level.setBlock(floor.above(), Blocks.FIRE.defaultBlockState(), Block.UPDATE_ALL);
            return;
        }

        if (random.nextFloat() < 0.7F) {
            level.setBlock(floor, randomBurntBlock(random), Block.UPDATE_CLIENTS);
        }
    }

    private static BlockState randomFireBase(RandomSource random) {
        if (random.nextFloat() < LASTING_FIRE) return Blocks.NETHERRACK.defaultBlockState();
        if (random.nextBoolean()) return Blocks.COARSE_DIRT.defaultBlockState();
        return Blocks.BLACKSTONE.defaultBlockState();
    }

    private static BlockState randomBurntBlock(RandomSource random) {
        float roll = random.nextFloat();
        if (roll < 0.35F) return Blocks.COARSE_DIRT.defaultBlockState();
        if (roll < 0.65F) return Blocks.BLACKSTONE.defaultBlockState();
        if (roll < 0.85F) return Blocks.BASALT.defaultBlockState();
        return Blocks.MAGMA_BLOCK.defaultBlockState();
    }

    private static void throwUpRim(ServerLevel level, BlockPos ground, RandomSource random) {
        for (int dx = -RADIUS - RIM; dx <= RADIUS + RIM; dx++) {
            for (int dz = -RADIUS - RIM; dz <= RADIUS + RIM; dz++) {
                double distance = Math.sqrt(dx * dx + dz * dz);
                boolean onRim = distance > RADIUS && distance <= RADIUS + RIM;
                if (!onRim || random.nextFloat() > 0.4F) continue;

                BlockPos place = surfaceNear(level, ground.offset(dx, 0, dz)).above();
                if (level.isOutsideBuildHeight(place) || !level.getBlockState(place).canBeReplaced()) continue;
                level.setBlock(place, randomRubble(random), Block.UPDATE_ALL);
            }
        }
    }

    private static BlockPos surfaceNear(ServerLevel level, BlockPos pos) {
        for (int i = 0; i < 6 && solid(level, pos.above()); i++) pos = pos.above();
        for (int i = 0; i < 6 && !solid(level, pos); i++) pos = pos.below();
        return pos;
    }

    private static BlockState randomRubble(RandomSource random) {
        if (random.nextBoolean()) return Blocks.COARSE_DIRT.defaultBlockState();
        if (random.nextBoolean()) return Blocks.GRAVEL.defaultBlockState();
        return Blocks.COBBLESTONE.defaultBlockState();
    }
}

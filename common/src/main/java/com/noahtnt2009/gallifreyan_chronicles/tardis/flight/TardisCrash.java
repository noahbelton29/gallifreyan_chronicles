package com.noahtnt2009.gallifreyan_chronicles.tardis.flight;

import java.util.UUID;

import java.util.Set;

import java.util.HashSet;

import net.minecraft.world.level.block.state.BlockState;

import net.minecraft.world.level.block.Blocks;

import net.minecraft.world.level.block.Block;

import net.minecraft.core.Direction;

import com.noahtnt2009.gallifreyan_chronicles.util.CrackedBlocks;


import com.noahtnt2009.gallifreyan_chronicles.block.entity.TardisExteriorBlockEntity;
import com.noahtnt2009.gallifreyan_chronicles.init.GCGameRules;
import com.noahtnt2009.gallifreyan_chronicles.network.TardisCrashRumblePayload;
import com.noahtnt2009.gallifreyan_chronicles.network.TardisDrumShakePayload;
import com.noahtnt2009.gallifreyan_chronicles.tardis.api.Tardis;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.mood.TardisAlarm;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.discovery.TardisDiscovery;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.TardisMindSystem;
import com.noahtnt2009.gallifreyan_chronicles.tardis.mind.TardisTrait;
import com.noahtnt2009.gallifreyan_chronicles.util.GCScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class TardisCrash {
    private static final int BUMP_TICKS = 8;
    private static final float CRASH_STRESS = 25f;
    private static final int SETTLE_DELAY_TICKS = 20;
    private static final int DAMAGE_RADIUS = 12;
    private static final int DAMAGE_HEIGHT = 6;
    private static final int CRACK_TRIES_PER_BUMP = 30;
    private static final int FIRE_TRIES_PER_BUMP = 16;
    private static final float IMPACT_SHAKE = 5.0F;

    private static final Set<UUID> CRASHING = new HashSet<>();

    private TardisCrash() {
    }

    public static boolean isCrashing(Tardis tardis) {
        return CRASHING.contains(tardis.id());
    }

    private static final float PROTECTIVE_SOFTENING = 0.5F;

    public static void reset() {
        CRASHING.clear();
    }

    public static void run(Tardis tardis, ServerLevel interior, BlockPos consolePos, int landingTicks) {
        CRASHING.add(tardis.id());
        TardisMindSystem.addStress(tardis, CRASH_STRESS);
        TardisDiscovery.trait(tardis, TardisTrait.PROTECTIVE);
        int flashes = landingTicks / TardisAlarm.FLASH_TICKS + 1;
        TardisAlarm.flashLoud(interior, flashes);

        Vec3 console = Vec3.atCenterOf(consolePos);
        burst(interior, console);
        TardisDrumShakePayload.broadcast(interior, console, IMPACT_SHAKE * protection(tardis));
        TardisCrashRumblePayload.broadcast(interior, console, landingTicks);
        for (int tick = 0; tick < landingTicks; tick += BUMP_TICKS) {
            float progress = tick / (float) landingTicks;
            GCScheduler.runLater(interior.getServer(), tick, () -> bump(tardis, interior, console, progress));
        }
        GCScheduler.runLater(interior.getServer(), landingTicks, () -> impact(tardis, interior, console));
        GCScheduler.runLater(interior.getServer(), landingTicks + SETTLE_DELAY_TICKS, () -> HaywireControls.settle(tardis));
    }

    private static void bump(Tardis tardis, ServerLevel level, Vec3 console, float progress) {
        RandomSource random = level.getRandom();
        HaywireControls.flick(tardis, random);
        if (level.getServer().getGameRules().get(GCGameRules.TARDIS_GRIEFING)) {
            damageInterior(level, console, random);
        }

        level.sendParticles(ParticleTypes.LARGE_SMOKE, console.x, console.y + 1, console.z, 12, 3.0, 1.5, 3.0, 0.02);
        level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, console.x, console.y + 0.5, console.z, 4, 4.0, 0.5, 4.0, 0.01);
        level.sendParticles(ParticleTypes.FLAME, console.x, console.y + 0.5, console.z, 30, 6.0, 1.0, 6.0, 0.02);
        level.sendParticles(ParticleTypes.LAVA, console.x, console.y + 0.5, console.z, 6, 5.0, 0.5, 5.0, 0.0);
    }

    /* smoke and fire blasting out of the console */
    private static void burst(ServerLevel level, Vec3 console) {
        double x = console.x;
        double y = console.y + 1.0;
        double z = console.z;
        level.sendParticles(ParticleTypes.EXPLOSION, x, y, z, 6, 1.0, 0.6, 1.0, 0.0);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, x, y, z, 120, 1.5, 1.0, 1.5, 0.25);
        level.sendParticles(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, x, y, z, 20, 0.8, 0.5, 0.8, 0.05);
        level.sendParticles(ParticleTypes.FLAME, x, y, z, 120, 1.2, 0.8, 1.2, 0.3);
        level.sendParticles(ParticleTypes.LAVA, x, y, z, 30, 1.0, 0.5, 1.0, 0.0);
        level.playSound(null, BlockPos.containing(console), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 2.0F, 0.6F);
    }

    private static void damageInterior(ServerLevel level, Vec3 console, RandomSource random) {
        for (int i = 0; i < CRACK_TRIES_PER_BUMP; i++) {
            crackBlock(level, randomInteriorSpot(console, random));
        }
        for (int i = 0; i < FIRE_TRIES_PER_BUMP; i++) {
            lightFire(level, randomInteriorSpot(console, random));
        }
    }

    private static BlockPos randomInteriorSpot(Vec3 console, RandomSource random) {
        int dx = random.nextInt(DAMAGE_RADIUS * 2 + 1) - DAMAGE_RADIUS;
        int dy = random.nextInt(DAMAGE_HEIGHT + 4) - 3;
        int dz = random.nextInt(DAMAGE_RADIUS * 2 + 1) - DAMAGE_RADIUS;
        return BlockPos.containing(console).offset(dx, dy, dz);
    }

    private static void crackBlock(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        Block cracked = CrackedBlocks.crackedOf(state.getBlock());
        if (cracked == null) return;
        level.setBlock(pos, cracked.defaultBlockState(), Block.UPDATE_ALL);
        level.sendParticles(ParticleTypes.POOF, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 4, 0.3, 0.3, 0.3, 0.02);
    }

    private static void lightFire(ServerLevel level, BlockPos pos) {
        BlockPos below = pos.below();
        boolean onFloor = level.getBlockState(pos).isAir()
                && level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
        if (onFloor) {
            level.setBlock(pos, Blocks.FIRE.defaultBlockState(), Block.UPDATE_ALL);
        }
    }


    private static void impact(Tardis tardis, ServerLevel interior, Vec3 console) {
        CRASHING.remove(tardis.id());
        if (interior.getServer().getGameRules().get(GCGameRules.TARDIS_GRIEFING)) {
            TardisCrater.blast(tardis);
        }
        interiorImpact(interior, console, protection(tardis));
        tardis.exteriorIfLoaded().ifPresent(TardisCrash::exteriorImpact);
        TardisDamage.markCrashed(tardis);
    }

    private static void interiorImpact(ServerLevel interior, Vec3 console, float protection) {
        interior.explode(null, console.x + 3, console.y + 1, console.z, 2.2F * protection, Level.ExplosionInteraction.NONE);
        interior.sendParticles(ParticleTypes.EXPLOSION_EMITTER, console.x, console.y + 1, console.z, 1, 0, 0, 0, 0);
        interior.sendParticles(ParticleTypes.LARGE_SMOKE, console.x, console.y + 1, console.z, 40, 4.0, 2.0, 4.0, 0.03);
        burst(interior, console);
        TardisDrumShakePayload.broadcast(interior, console, IMPACT_SHAKE * protection);
    }

    /* protective tardises soften the crash for the people inside */
    private static float protection(Tardis tardis) {
        return TardisMindSystem.mind(tardis).has(TardisTrait.PROTECTIVE) ? PROTECTIVE_SOFTENING : 1F;
    }

    private static void exteriorImpact(TardisExteriorBlockEntity exterior) {
        if (!(exterior.getLevel() instanceof ServerLevel outside)) return;
        Vec3 at = Vec3.atBottomCenterOf(exterior.getBlockPos());
        outside.sendParticles(ParticleTypes.EXPLOSION_EMITTER, at.x, at.y + 1, at.z, 1, 0, 0, 0, 0);
        outside.sendParticles(ParticleTypes.LARGE_SMOKE, at.x, at.y + 1, at.z, 50, 1.5, 1.5, 1.5, 0.05);
        outside.sendParticles(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, at.x, at.y + 2, at.z, 10, 0.6, 0.5, 0.6, 0.02);
        outside.playSound(null, exterior.getBlockPos(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 4.0F, 0.7F);
        TardisDrumShakePayload.broadcast(outside, at, 2.0F);
    }


}

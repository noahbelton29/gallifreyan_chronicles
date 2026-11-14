package uk.nbforge.gallifreyan_chronicles.util;

import uk.nbforge.gallifreyan_chronicles.core.GCSounds;
import uk.nbforge.gallifreyan_chronicles.network.tardis.TardisDrumShakePayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

/* sparks made of fire with a bit of smoke, and their sounds. small crackles, big bursts and full blasts.
   every spark gives the screen a jolt, bigger sparks jolt harder */
public final class GCSparkUtil {
    private static final float SMALL_SHAKE = 2.0f;
    private static final float BIG_SHAKE = 3.0f;
    private static final float BLAST_SHAKE = 4.5f;

    private GCSparkUtil() {
    }

    /* a little crackle of sparks */
    public static void small(ServerLevel level, Vec3 at, int amount) {
        RandomSource random = level.getRandom();
        level.sendParticles(ParticleTypes.FLAME, at.x, at.y, at.z, amount, 0.15, 0.1, 0.15, 0.08);
        if (random.nextInt(3) == 0) {
            level.sendParticles(ParticleTypes.SMOKE, at.x, at.y, at.z, Math.max(1, amount / 2), 0.1, 0.1, 0.1, 0.02);
        }
        play(level, at, GCSounds.SPARK, 0.5f + random.nextFloat() * 0.3f, 0.9f + random.nextFloat() * 0.4f);
        TardisDrumShakePayload.broadcast(level, at, SMALL_SHAKE);
    }

    /* a burst of sparks that crackles for a moment after */
    public static void big(ServerLevel level, Vec3 at) {
        RandomSource random = level.getRandom();
        level.sendParticles(ParticleTypes.FLAME, at.x, at.y, at.z, 35, 0.3, 0.3, 0.3, 0.2);
        level.sendParticles(ParticleTypes.LAVA, at.x, at.y, at.z, 3, 0.2, 0.2, 0.2, 0.0);
        level.sendParticles(ParticleTypes.SMOKE, at.x, at.y, at.z, 10, 0.3, 0.3, 0.3, 0.03);
        play(level, at, GCSounds.SPARK_EXPLOSION, 1.0f, 0.85f + random.nextFloat() * 0.3f);
        TardisDrumShakePayload.broadcast(level, at, BIG_SHAKE);
        crackleAfter(level, at, 2);
    }

    /* an explosion with sparks flying everywhere */
    public static void blast(ServerLevel level, Vec3 at) {
        RandomSource random = level.getRandom();
        level.sendParticles(ParticleTypes.FLAME, at.x, at.y, at.z, 80, 0.8, 0.6, 0.8, 0.3);
        level.sendParticles(ParticleTypes.LAVA, at.x, at.y, at.z, 10, 0.6, 0.4, 0.6, 0.0);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, at.x, at.y, at.z, 30, 0.8, 0.6, 0.8, 0.05);
        play(level, at, GCSounds.EXPLOSION, 1.6f, 0.8f + random.nextFloat() * 0.2f);
        play(level, at, GCSounds.SPARK_EXPLOSION, 1.2f, 0.8f + random.nextFloat() * 0.2f);
        TardisDrumShakePayload.broadcast(level, at, BLAST_SHAKE);
        crackleAfter(level, at, 4);
    }

    /* quieter crackles trailing off after a burst */
    private static void crackleAfter(ServerLevel level, Vec3 at, int times) {
        RandomSource random = level.getRandom();
        int delay = 0;
        for (int i = 0; i < times; i++) {
            delay += 5 + random.nextInt(8);
            float volume = 0.5f - i * 0.08f;
            GCSchedulerUtil.runLater(level.getServer(), delay, () -> {
                level.sendParticles(ParticleTypes.FLAME, at.x, at.y, at.z, 5, 0.25, 0.15, 0.25, 0.06);
                play(level, at, GCSounds.SPARK, Math.max(0.15f, volume), 1.0f + level.getRandom().nextFloat() * 0.4f);
                TardisDrumShakePayload.broadcast(level, at, SMALL_SHAKE * 0.5f);
            });
        }
    }

    private static void play(ServerLevel level, Vec3 at, SoundEvent sound, float volume, float pitch) {
        level.playSound(null, BlockPos.containing(at), sound, SoundSource.BLOCKS, volume, pitch);
    }
}

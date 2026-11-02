package com.noahtnt2009.gallifreyan_chronicles.tardis.exterior;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

public final class TakeoffWind {
    private static final int MAX_DUST = 10;
    private static final double DUST_RADIUS = 0.95;
    private static final float DUST_SPEED = 0.2f;
    private static final double CLOUD_RADIUS = 0.6;
    private static final float CLOUD_SPEED = 0.08f;
    private static final float END_PROGRESS = 0.92f;

    private TakeoffWind() {
    }

    public static void tick(ServerLevel level, BlockPos pos, boolean inFlight, boolean fullyInvisible,
                            long fadeStartTick, long fadeDuration) {
        long now = level.getGameTime();
        float progress = Math.clamp((now - fadeStartTick) / (float) fadeDuration, 0.0f, 1.0f);

        float strength;
        if (inFlight) {
            if (fullyInvisible || progress >= END_PROGRESS) return;
            strength = 1.0f - progress / END_PROGRESS;
            strength *= strength;
        } else {
            if (progress >= 1.0f || fadeStartTick > now) return;
            strength = progress * progress;
        }

        if (now % 3L != 0L) return;

        double cx = pos.getX() + 0.5;
        double cy = pos.getY() + 0.05;
        double cz = pos.getZ() + 0.5;

        BlockState ground = level.getBlockState(pos.below());
        ParticleOptions dust = ground.isAir() || ground.getRenderShape() == RenderShape.INVISIBLE
                ? ParticleTypes.CLOUD
                : new BlockParticleOption(ParticleTypes.BLOCK, ground);

        var random = level.getRandom();
        double spin = now * 0.15;

        int dustPoints = Math.max(inFlight ? 2 : 1, Math.round(MAX_DUST * strength));
        float dustSpeed = DUST_SPEED * (0.5f + 0.5f * strength);
        for (int i = 0; i < dustPoints; i++) {
            double angle = spin + (Math.PI * 2.0 * i) / dustPoints + (random.nextDouble() - 0.5) * 0.3;
            double dx = Math.cos(angle);
            double dz = Math.sin(angle);
            level.sendParticles(dust,
                    cx + dx * DUST_RADIUS, cy, cz + dz * DUST_RADIUS,
                    0, dx, 0.0, dz, dustSpeed);
        }

        int clouds = strength > 0.5f ? 2 : 1;
        for (int i = 0; i < clouds; i++) {
            double angle = random.nextDouble() * Math.PI * 2.0;
            double dx = Math.cos(angle);
            double dz = Math.sin(angle);
            level.sendParticles(ParticleTypes.CLOUD,
                    cx + dx * CLOUD_RADIUS, cy, cz + dz * CLOUD_RADIUS,
                    0, dx, 0.0, dz, CLOUD_SPEED * (0.6f + 0.4f * strength));
        }
    }
}

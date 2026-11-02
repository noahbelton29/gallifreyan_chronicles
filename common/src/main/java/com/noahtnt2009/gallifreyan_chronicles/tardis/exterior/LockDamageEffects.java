package com.noahtnt2009.gallifreyan_chronicles.tardis.exterior;

import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.system.SubsystemSystem;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public final class LockDamageEffects {
    private LockDamageEffects() {
    }

    public static void tick(ServerLevel level, Vec3 pos, float health) {
        if (health >= SubsystemSystem.SMOKE_THRESHOLD) {
            return;
        }

        var random = level.getRandom();

        float smoke = (SubsystemSystem.SMOKE_THRESHOLD - health) / SubsystemSystem.SMOKE_THRESHOLD;
        if (random.nextFloat() < 0.04f + 0.40f * smoke) {
            level.sendParticles(ParticleTypes.SMOKE,
                    pos.x, pos.y, pos.z, 1, 0.06, 0.06, 0.06, 0.01);
        }

        if (health <= 0f) {
            level.sendParticles(ParticleTypes.FLAME,
                    pos.x, pos.y, pos.z, 3, 0.12, 0.12, 0.12, 0.02);
            if (random.nextFloat() < 0.35f) {
                level.sendParticles(ParticleTypes.LARGE_SMOKE,
                        pos.x, pos.y + 0.1, pos.z, 1, 0.08, 0.08, 0.08, 0.03);
            }
        } else if (health < SubsystemSystem.FIRE_THRESHOLD) {
            float fire = (SubsystemSystem.FIRE_THRESHOLD - health) / SubsystemSystem.FIRE_THRESHOLD;
            if (random.nextFloat() < 0.03f + 0.15f * fire) {
                level.sendParticles(ParticleTypes.FLAME,
                        pos.x, pos.y, pos.z, 1, 0.05, 0.05, 0.05, 0.01);
            }
        }
    }
}

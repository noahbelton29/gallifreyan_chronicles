package uk.nbforge.gallifreyan_chronicles.tardis.subsystem;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import uk.nbforge.gallifreyan_chronicles.core.block_entity.TardisExteriorBlockEntity;

public final class LockingMechanismSubsystem extends TardisSubsystem {
    public static final float JAM_THRESHOLD = 80f;
    public static final float LIGHT_JAM_THRESHOLD = 40f;
    public static final float HEAVY_JAM_THRESHOLD = 30f;
    public static final float SMOKE_THRESHOLD = 30f;
    public static final float FIRE_THRESHOLD = 15f;
    public static final float KEY_BLOCK_THRESHOLD = 15f;
    private static final float OCCASIONAL_JAM_CHANCE_START = 0.03f;
    private static final float LIGHT_JAM_CHANCE = 0.15f;
    private static final float HEAVY_JAM_CHANCE_START = 0.50f;

    public LockingMechanismSubsystem() {
        super("locking_mechanism", "door_lock");
    }

    @Override
    public boolean isFaulty(float health) {
        return health < JAM_THRESHOLD;
    }

    public boolean acceptsKey(float health) {
        return health >= KEY_BLOCK_THRESHOLD;
    }

    public float jamChance(float health) {
        if (health >= JAM_THRESHOLD) return 0f;
        if (health <= 0f) return 1f;
        if (health >= LIGHT_JAM_THRESHOLD) {
            float t = (JAM_THRESHOLD - health) / (JAM_THRESHOLD - LIGHT_JAM_THRESHOLD);
            return OCCASIONAL_JAM_CHANCE_START + (LIGHT_JAM_CHANCE - OCCASIONAL_JAM_CHANCE_START) * t;
        }
        if (health >= HEAVY_JAM_THRESHOLD) return LIGHT_JAM_CHANCE;
        float t = (HEAVY_JAM_THRESHOLD - health) / HEAVY_JAM_THRESHOLD;
        return HEAVY_JAM_CHANCE_START + (1f - HEAVY_JAM_CHANCE_START) * t;
    }

    public boolean rollJam(float health, RandomSource random) {
        float chance = jamChance(health);
        if (chance <= 0f) return false;
        if (chance >= 1f) return true;
        return random.nextFloat() < chance;
    }

    @Override
    public void tick(ServerLevel level, TardisExteriorBlockEntity exterior, float health) {
        if (health >= SMOKE_THRESHOLD) {
            return;
        }

        Vec3 pos = exterior.lock().getKeyWorldPos();
        var random = level.getRandom();

        float smoke = (SMOKE_THRESHOLD - health) / SMOKE_THRESHOLD;
        if (random.nextFloat() < 0.04f + 0.40f * smoke) {
            level.sendParticles(ParticleTypes.SMOKE, pos.x, pos.y, pos.z, 1, 0.06, 0.06, 0.06, 0.01);
        }

        if (health <= 0f) {
            level.sendParticles(ParticleTypes.FLAME, pos.x, pos.y, pos.z, 3, 0.12, 0.12, 0.12, 0.02);
            if (random.nextFloat() < 0.35f) {
                level.sendParticles(ParticleTypes.LARGE_SMOKE, pos.x, pos.y + 0.1, pos.z, 1, 0.08, 0.08, 0.08, 0.03);
            }
        } else if (health < FIRE_THRESHOLD) {
            float fire = (FIRE_THRESHOLD - health) / FIRE_THRESHOLD;
            if (random.nextFloat() < 0.03f + 0.15f * fire) {
                level.sendParticles(ParticleTypes.FLAME, pos.x, pos.y, pos.z, 1, 0.05, 0.05, 0.05, 0.01);
            }
        }
    }
}

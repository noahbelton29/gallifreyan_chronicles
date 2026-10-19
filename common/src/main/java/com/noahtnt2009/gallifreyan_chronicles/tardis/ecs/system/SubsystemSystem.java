package com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.system;

import com.noahtnt2009.gallifreyan_chronicles.ecs.Entity;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.TardisComponentTypes;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.component.HealthComponent;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.component.SubsystemsComponent;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.component.TardisSubsystem;
import net.minecraft.util.RandomSource;

public final class SubsystemSystem {
    public static final float JAM_THRESHOLD = 80f;
    public static final float LIGHT_JAM_THRESHOLD = 40f;
    public static final float HEAVY_JAM_THRESHOLD = 30f;
    public static final float SMOKE_THRESHOLD = 30f;
    public static final float FIRE_THRESHOLD = 15f;
    public static final float KEY_BLOCK_THRESHOLD = 15f;
    private static final float OCCASIONAL_JAM_CHANCE_START = 0.03f;
    private static final float LIGHT_JAM_CHANCE = 0.15f;
    private static final float HEAVY_JAM_CHANCE_START = 0.50f;

    private SubsystemSystem() {
    }

    public static HealthComponent healthComponentOf(Entity entity, TardisSubsystem subsystem) {
        return entity.get(TardisComponentTypes.SUBSYSTEMS).healthOf(subsystem);
    }

    public static float healthOf(Entity entity, TardisSubsystem subsystem) {
        return healthComponentOf(entity, subsystem).value();
    }

    private static void store(Entity entity, TardisSubsystem subsystem, HealthComponent health) {
        SubsystemsComponent current = entity.get(TardisComponentTypes.SUBSYSTEMS);
        entity.set(TardisComponentTypes.SUBSYSTEMS, current.withHealth(subsystem, health));
    }

    public static void setHealth(Entity entity, TardisSubsystem subsystem, float health) {
        store(entity, subsystem, healthComponentOf(entity, subsystem).withValue(health));
    }

    public static void damage(Entity entity, TardisSubsystem subsystem, float amount) {
        store(entity, subsystem, healthComponentOf(entity, subsystem).damaged(amount));
    }

    public static void repair(Entity entity, TardisSubsystem subsystem, float amount) {
        store(entity, subsystem, healthComponentOf(entity, subsystem).repaired(amount));
    }

    public static boolean acceptsKey(Entity entity, TardisSubsystem subsystem) {
        return healthOf(entity, subsystem) >= KEY_BLOCK_THRESHOLD;
    }

    public static boolean isFaulty(Entity entity, TardisSubsystem subsystem) {
        return healthOf(entity, subsystem) < JAM_THRESHOLD;
    }

    public static float jamChance(float health) {
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

    public static boolean rollJam(Entity entity, TardisSubsystem subsystem, RandomSource random) {
        float chance = jamChance(healthOf(entity, subsystem));
        if (chance <= 0f) return false;
        if (chance >= 1f) return true;
        return random.nextFloat() < chance;
    }
}

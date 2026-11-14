package uk.nbforge.gallifreyan_chronicles.core.effect;

import uk.nbforge.gallifreyan_chronicles.util.GCUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/* choking on smoke. hurts every 2 seconds, each level up hurts half a heart more */
public class ChokingEffect extends MobEffect {
    public static final ResourceKey<DamageType> SMOKE = ResourceKey.create(Registries.DAMAGE_TYPE,
            GCUtil.modIdentifier("smoke"));
    private static final int HURT_EVERY_TICKS = 40;
    private static final float BASE_DAMAGE = 1.0f;
    private static final float DAMAGE_PER_LEVEL = 1.0f;

    public ChokingEffect() {
        super(MobEffectCategory.HARMFUL, 0x5A5A5E);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % HURT_EVERY_TICKS == 0;
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
        DamageSource smoke = new DamageSource(level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(SMOKE));
        entity.hurtServer(level, smoke, BASE_DAMAGE + amplifier * DAMAGE_PER_LEVEL);
        return true;
    }
}

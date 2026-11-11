package com.noahtnt2009.gallifreyan_chronicles.handler;

import com.noahtnt2009.gallifreyan_chronicles.util.TaraniumArmor;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.NonNull;

public class TaraniumLavaDamageHandler implements ServerLivingEntityEvents.AllowDamage {
    @Override
    public boolean allowDamage(@NonNull LivingEntity entity, @NonNull DamageSource source, float amount) {
        return !(entity instanceof Player player && TaraniumArmor.blocksFireDamage(player, source));
    }
}

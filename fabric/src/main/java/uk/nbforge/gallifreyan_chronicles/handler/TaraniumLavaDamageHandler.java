package uk.nbforge.gallifreyan_chronicles.handler;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.NonNull;
import uk.nbforge.gallifreyan_chronicles.util.TaraniumArmorUtil;

public class TaraniumLavaDamageHandler implements ServerLivingEntityEvents.AllowDamage {
    @Override
    public boolean allowDamage(@NonNull LivingEntity entity, @NonNull DamageSource source, float amount) {
        return !(entity instanceof Player player && TaraniumArmorUtil.blocksFireDamage(player, source));
    }
}

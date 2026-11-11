package com.noahtnt2009.gallifreyan_chronicles.event_subscriber;

import com.noahtnt2009.gallifreyan_chronicles.util.TaraniumArmor;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

public class TaraniumLavaDamageHandler {
    @SubscribeEvent
    public void onLavaDamage(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof Player player && TaraniumArmor.blocksFireDamage(player, event.getSource())) {
            event.setCanceled(true);
        }
    }
}

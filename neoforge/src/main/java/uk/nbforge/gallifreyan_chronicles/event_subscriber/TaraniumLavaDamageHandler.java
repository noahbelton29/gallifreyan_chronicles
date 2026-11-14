package uk.nbforge.gallifreyan_chronicles.event_subscriber;

import uk.nbforge.gallifreyan_chronicles.util.TaraniumArmorUtil;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

public class TaraniumLavaDamageHandler {
    @SubscribeEvent
    public void onLavaDamage(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof Player player && TaraniumArmorUtil.blocksFireDamage(player, event.getSource())) {
            event.setCanceled(true);
        }
    }
}

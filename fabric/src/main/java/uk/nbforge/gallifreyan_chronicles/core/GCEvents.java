package uk.nbforge.gallifreyan_chronicles.core;

import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.handler.TaraniumLavaDamageHandler;
import uk.nbforge.gallifreyan_chronicles.tardis.security.TardisTrust;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;

public class GCEvents {
    public static void registerEvents() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register(new TaraniumLavaDamageHandler());
        UseEntityCallback.EVENT.register((player, level, hand, entity, hitResult) ->
                TardisTrust.interact(player, level, hand, entity));
        Constants.LOG.info("Registered GC Events");
    }
}
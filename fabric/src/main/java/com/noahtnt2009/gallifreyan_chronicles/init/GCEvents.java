package com.noahtnt2009.gallifreyan_chronicles.init;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.handler.TaraniumLavaDamageHandler;
import com.noahtnt2009.gallifreyan_chronicles.tardis.security.TardisTrust;
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
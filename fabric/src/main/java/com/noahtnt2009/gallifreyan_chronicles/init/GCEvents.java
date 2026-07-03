package com.noahtnt2009.gallifreyan_chronicles.init;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.handler.TaraniumLavaDamageHandler;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;

public class GCEvents {
    public static void registerEvents() {
        Constants.LOG.info("Registered GC Events");
        ServerLivingEntityEvents.ALLOW_DAMAGE.register(new TaraniumLavaDamageHandler());
    }
}
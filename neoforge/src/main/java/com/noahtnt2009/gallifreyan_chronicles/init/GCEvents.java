package com.noahtnt2009.gallifreyan_chronicles.init;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.event_subscriber.GCCraftingHandler;
import com.noahtnt2009.gallifreyan_chronicles.event_subscriber.TaraniumLavaDamageHandler;
import net.neoforged.neoforge.common.NeoForge;

public class GCEvents {
    public static void registerEvents() {
        NeoForge.EVENT_BUS.register(new TaraniumLavaDamageHandler());
        NeoForge.EVENT_BUS.register(new GCCraftingHandler());

        Constants.LOG.info("Registered GC Events (NeoForge)");
    }
}
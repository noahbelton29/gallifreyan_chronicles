package uk.nbforge.gallifreyan_chronicles.core;

import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.event_subscriber.GCCraftingHandler;
import uk.nbforge.gallifreyan_chronicles.event_subscriber.TaraniumLavaDamageHandler;
import uk.nbforge.gallifreyan_chronicles.event_subscriber.TardisTrustHandler;
import net.neoforged.neoforge.common.NeoForge;

public class GCEvents {
    public static void registerEvents() {
        NeoForge.EVENT_BUS.register(new TaraniumLavaDamageHandler());
        NeoForge.EVENT_BUS.register(new GCCraftingHandler());
        NeoForge.EVENT_BUS.register(new TardisTrustHandler());

        Constants.LOG.info("Registered GC Events (NeoForge)");
    }
}
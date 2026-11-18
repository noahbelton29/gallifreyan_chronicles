package uk.nbforge.gallifreyan_chronicles.core;

import net.neoforged.neoforge.common.NeoForge;
import uk.nbforge.gallifreyan_chronicles.event_subscriber.DoorRoundelHandler;
import uk.nbforge.gallifreyan_chronicles.event_subscriber.GCCraftingHandler;
import uk.nbforge.gallifreyan_chronicles.event_subscriber.TaraniumLavaDamageHandler;
import uk.nbforge.gallifreyan_chronicles.event_subscriber.TardisTrustHandler;
import uk.nbforge.gallifreyan_chronicles.util.GCLog;

public class GCEvents {
    public static void registerEvents() {
        NeoForge.EVENT_BUS.register(new TaraniumLavaDamageHandler());
        NeoForge.EVENT_BUS.register(new GCCraftingHandler());
        NeoForge.EVENT_BUS.register(new TardisTrustHandler());
        NeoForge.EVENT_BUS.register(new DoorRoundelHandler());

        GCLog.CORE.info("Registered GC Events (NeoForge)");
    }
}

package uk.nbforge.gallifreyan_chronicles.core;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import uk.nbforge.gallifreyan_chronicles.command.GCCommands;
import uk.nbforge.gallifreyan_chronicles.util.GCLog;

public class GCFabricCommands {
    public static void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, _, _) -> dispatcher.register(GCCommands.root()));

        GCLog.CORE.info("Registered GC Commands (Fabric)");
    }
}

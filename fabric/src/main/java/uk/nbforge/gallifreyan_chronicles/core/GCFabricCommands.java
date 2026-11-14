package uk.nbforge.gallifreyan_chronicles.core;

import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.command.GCCommands;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class GCFabricCommands {
    public static void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, _, _) -> dispatcher.register(GCCommands.root()));

        Constants.LOG.info("Registered GC Commands (Fabric)");
    }
}

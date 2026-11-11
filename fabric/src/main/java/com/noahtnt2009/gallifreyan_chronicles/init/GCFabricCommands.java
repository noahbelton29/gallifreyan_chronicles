package com.noahtnt2009.gallifreyan_chronicles.init;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.init.GCCommands;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class GCFabricCommands {
    public static void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, _, _) -> dispatcher.register(GCCommands.root()));

        Constants.LOG.info("Registered GC Commands (Fabric)");
    }
}

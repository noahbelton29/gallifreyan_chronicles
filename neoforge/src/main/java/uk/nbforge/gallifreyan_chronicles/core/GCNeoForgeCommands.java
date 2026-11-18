package uk.nbforge.gallifreyan_chronicles.core;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.command.GCCommands;
import uk.nbforge.gallifreyan_chronicles.util.GCLog;

@EventBusSubscriber(modid = Constants.MOD_ID)
public class GCNeoForgeCommands {
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(GCCommands.root());

        GCLog.CORE.info("Registered GC Commands (NeoForge)");
    }
}

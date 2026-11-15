package uk.nbforge.gallifreyan_chronicles.compat.replay;

import uk.nbforge.gallifreyan_chronicles.platform.Services;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;

/**
   Tells you if we are watching a Flashback replay with Immersive Portals installed.
   The replay runs on a Flashback server inside the game so we check its class name.
*/
public final class PortalReplay {
    private static final boolean PORTALS = Services.PLATFORM.isModLoaded("seamlessportals");

    private PortalReplay() {
    }

    public static boolean active() {
        if (!PORTALS) return false;
        MinecraftServer server = Minecraft.getInstance().getSingleplayerServer();
        return server != null && server.getClass().getName().startsWith("com.moulberry.flashback.");
    }
}

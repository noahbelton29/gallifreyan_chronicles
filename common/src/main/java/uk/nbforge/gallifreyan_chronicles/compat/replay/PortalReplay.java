package uk.nbforge.gallifreyan_chronicles.compat.replay;

import uk.nbforge.gallifreyan_chronicles.platform.Services;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;

/* tells you if we're watching a flashback replay with immersive portals installed.
   the replay runs on a flashback server inside the game, so we check its class name */
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

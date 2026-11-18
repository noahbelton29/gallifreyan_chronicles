package uk.nbforge.gallifreyan_chronicles.client.compat.replay;

import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import uk.nbforge.gallifreyan_chronicles.platform.Services;

public final class PortalReplay {
    private static final boolean PORTALS = Services.PLATFORM.isModLoaded("seamlessportals");

    private PortalReplay() {}

    public static boolean active() {
        if (!PORTALS) return false;
        MinecraftServer server = Minecraft.getInstance().getSingleplayerServer();
        return server != null && server.getClass().getName().startsWith("com.moulberry.flashback.");
    }
}

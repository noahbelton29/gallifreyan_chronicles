package uk.nbforge.gallifreyan_chronicles.client.renderer.util;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;

/** Client only helper kept in its own class so dedicated servers never load Minecraft through the door block. */
public final class RemoteDoorView {
    private RemoteDoorView() {}

    public static boolean isSeenThroughPortal(Level level) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return false;
        return !mc.player.level().dimension().equals(level.dimension());
    }
}

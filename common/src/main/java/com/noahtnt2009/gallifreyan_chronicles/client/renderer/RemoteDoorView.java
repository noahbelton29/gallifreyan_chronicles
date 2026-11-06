package com.noahtnt2009.gallifreyan_chronicles.client.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;

/**
 * Client-only helper kept in its own class so dedicated servers never load {@link Minecraft} through the door block.
 */
public final class RemoteDoorView {
    private RemoteDoorView() {
    }

    /**
     * True when {@code level} is a different dimension from the one the local player is actually in, i.e. the
     * block is being looked at through a portal. The interior door is then not meant to be targeted or outlined.
     */
    public static boolean isSeenThroughPortal(Level level) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return false;
        return !mc.player.level().dimension().equals(level.dimension());
    }
}

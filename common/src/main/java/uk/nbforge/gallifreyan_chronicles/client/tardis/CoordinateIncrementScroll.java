package uk.nbforge.gallifreyan_chronicles.client.tardis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.world.phys.EntityHitResult;
import uk.nbforge.gallifreyan_chronicles.core.GCTardisControls;
import uk.nbforge.gallifreyan_chronicles.core.entity.TardisControlEntity;
import uk.nbforge.gallifreyan_chronicles.network.tardis.CoordinateIncrementScrollPayload;

public final class CoordinateIncrementScroll {
    private CoordinateIncrementScroll() {}

    /** true if the scroll was used up on the coordinate step control, so the hotbar shouldn't move */
    public static boolean onScroll(double scrollY) {
        if (scrollY == 0.0) return false;

        Minecraft mc = Minecraft.getInstance();
        if (mc.gui.screen() != null || mc.player == null || !mc.player.isShiftKeyDown()) return false;
        if (!(mc.hitResult instanceof EntityHitResult hit)) return false;
        if (!(hit.getEntity() instanceof TardisControlEntity control)) return false;
        if (!GCTardisControls.COORDINATE_STEP_CONTROL.equals(control.getControlId())) return false;

        ClientPacketListener connection = mc.getConnection();
        if (connection == null) return false;
        int direction = scrollY > 0.0 ? 1 : -1;
        connection.send(
                new ServerboundCustomPayloadPacket(new CoordinateIncrementScrollPayload(control.getId(), direction)));
        return true;
    }
}

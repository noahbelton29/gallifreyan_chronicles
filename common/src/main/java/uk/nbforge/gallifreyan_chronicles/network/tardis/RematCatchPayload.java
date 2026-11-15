package uk.nbforge.gallifreyan_chronicles.network.tardis;

import uk.nbforge.gallifreyan_chronicles.client.tardis.RematCatchClient;
import uk.nbforge.gallifreyan_chronicles.util.GCUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

/**
   A TARDIS materialised around a player and pulled them in.
   self is that player, who sees the interior fade in around them with box and floorY.
   Everyone else in the interior sees that player fade in like the TARDIS does.
*/
public record RematCatchPayload(UUID player, String fadeType, int elapsedTicks, int durationTicks,
                                boolean self, BlockPos boxMin, BlockPos boxMax, int floorY)
        implements CustomPacketPayload {

    public static final Type<RematCatchPayload> TYPE = new Type<>(GCUtil.modIdentifier("remat_catch"));

    public void apply() {
        RematCatchClient.start(this);
    }

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

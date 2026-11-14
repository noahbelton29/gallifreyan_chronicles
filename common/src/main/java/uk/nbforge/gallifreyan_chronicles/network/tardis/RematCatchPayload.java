package uk.nbforge.gallifreyan_chronicles.network.tardis;

import uk.nbforge.gallifreyan_chronicles.client.tardis.RematCatchClient;
import uk.nbforge.gallifreyan_chronicles.util.GCUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

/* a tardis materialised around a player and pulled them in.
   self: that player, who sees the interior fade in around them (box, floorY).
   everyone else in the interior: that player fades in like the tardis does */
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

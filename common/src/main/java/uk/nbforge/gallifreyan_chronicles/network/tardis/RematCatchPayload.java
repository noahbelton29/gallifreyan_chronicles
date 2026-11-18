package uk.nbforge.gallifreyan_chronicles.network.tardis;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jspecify.annotations.NonNull;
import uk.nbforge.gallifreyan_chronicles.client.tardis.RematCatchClient;
import uk.nbforge.gallifreyan_chronicles.util.GCUtil;

/** self is the caught player, they see the interior fade in around them using box and floorY */
public record RematCatchPayload(
        UUID player,
        String fadeType,
        int elapsedTicks,
        int durationTicks,
        boolean self,
        BlockPos boxMin,
        BlockPos boxMax,
        int floorY)
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

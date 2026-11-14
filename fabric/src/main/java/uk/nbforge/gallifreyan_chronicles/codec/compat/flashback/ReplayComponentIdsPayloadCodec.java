package uk.nbforge.gallifreyan_chronicles.codec.compat.flashback;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import uk.nbforge.gallifreyan_chronicles.compat.flashback.ReplayComponentIds;

public final class ReplayComponentIdsPayloadCodec {
    public static final StreamCodec<FriendlyByteBuf, ReplayComponentIds.Payload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list()), ReplayComponentIds.Payload::ids,
            Identifier.STREAM_CODEC.apply(ByteBufCodecs.list()), ReplayComponentIds.Payload::names,
            ReplayComponentIds.Payload::new);

    private ReplayComponentIdsPayloadCodec() {
    }
}

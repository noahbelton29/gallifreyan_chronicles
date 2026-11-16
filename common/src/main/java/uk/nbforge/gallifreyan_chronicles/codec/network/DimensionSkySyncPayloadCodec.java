package uk.nbforge.gallifreyan_chronicles.codec.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import uk.nbforge.gallifreyan_chronicles.codec.sky.data.CelestialBodyCodec;
import uk.nbforge.gallifreyan_chronicles.codec.sky.data.DimensionSkyCodec;
import uk.nbforge.gallifreyan_chronicles.network.DimensionSkySyncPayload;

public final class DimensionSkySyncPayloadCodec {
    public static final StreamCodec<FriendlyByteBuf, DimensionSkySyncPayload> STREAM_CODEC = StreamCodec.composite(
            CelestialBodyCodec.STREAM_CODEC.apply(ByteBufCodecs.list()),
            DimensionSkySyncPayload::bodies,
            DimensionSkyCodec.STREAM_CODEC.apply(ByteBufCodecs.list()),
            DimensionSkySyncPayload::skies,
            DimensionSkySyncPayload::new);

    private DimensionSkySyncPayloadCodec() {}
}

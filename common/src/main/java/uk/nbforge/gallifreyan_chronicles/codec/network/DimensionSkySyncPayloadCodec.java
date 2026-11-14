package uk.nbforge.gallifreyan_chronicles.codec.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import uk.nbforge.gallifreyan_chronicles.network.DimensionSkySyncPayload;
import uk.nbforge.gallifreyan_chronicles.sky.data.CelestialBody;
import uk.nbforge.gallifreyan_chronicles.sky.data.DimensionSky;
import uk.nbforge.gallifreyan_chronicles.codec.sky.data.CelestialBodyCodec;
import uk.nbforge.gallifreyan_chronicles.codec.sky.data.DimensionSkyCodec;

public final class DimensionSkySyncPayloadCodec {
    public static final StreamCodec<FriendlyByteBuf, DimensionSkySyncPayload> STREAM_CODEC = StreamCodec.composite(
            CelestialBodyCodec.STREAM_CODEC.apply(ByteBufCodecs.list()), DimensionSkySyncPayload::bodies,
            DimensionSkyCodec.STREAM_CODEC.apply(ByteBufCodecs.list()), DimensionSkySyncPayload::skies,
            DimensionSkySyncPayload::new
    );

    private DimensionSkySyncPayloadCodec() {
    }
}

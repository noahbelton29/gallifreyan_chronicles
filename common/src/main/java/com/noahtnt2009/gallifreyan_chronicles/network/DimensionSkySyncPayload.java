package com.noahtnt2009.gallifreyan_chronicles.network;

import com.noahtnt2009.gallifreyan_chronicles.client.sky.data.DimensionSky;
import com.noahtnt2009.gallifreyan_chronicles.client.sky.data.DimensionSkyRegistry;
import com.noahtnt2009.gallifreyan_chronicles.util.GCUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jspecify.annotations.NonNull;

import java.util.List;

public record DimensionSkySyncPayload(List<DimensionSky> skies) implements CustomPacketPayload {
    public static final Type<DimensionSkySyncPayload> TYPE =
            new Type<>(GCUtils.of("dimension_sky_sync"));

    public static final StreamCodec<FriendlyByteBuf, DimensionSkySyncPayload> STREAM_CODEC = StreamCodec.composite(
            DimensionSky.STREAM_CODEC.apply(ByteBufCodecs.list()), DimensionSkySyncPayload::skies,
            DimensionSkySyncPayload::new
    );

    public static DimensionSkySyncPayload create() {
        return new DimensionSkySyncPayload(List.copyOf(DimensionSkyRegistry.getAll()));
    }

    public void apply() {
        GCSyncPayload.apply(skies, DimensionSkyRegistry::clear, DimensionSkyRegistry::register, "Dimension Sky");
    }

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

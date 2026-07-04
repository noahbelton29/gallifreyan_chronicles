package com.noahtnt2009.gallifreyan_chronicles.network;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.client.sky.data.DimensionSky;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.List;

public record DimensionSkySyncPayload(List<DimensionSky> skies) implements CustomPacketPayload {
    public static final Type<DimensionSkySyncPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "dimension_sky_sync"));

    public static final StreamCodec<FriendlyByteBuf, DimensionSkySyncPayload> STREAM_CODEC = StreamCodec.composite(
            DimensionSky.STREAM_CODEC.apply(ByteBufCodecs.list()),
            DimensionSkySyncPayload::skies,
            DimensionSkySyncPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
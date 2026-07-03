package com.noahtnt2009.gallifreyan_chronicles.network;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.TardisExterior;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.List;

public record TardisExteriorSyncPayload(List<TardisExterior> exteriors, String defaultId) implements CustomPacketPayload {
    public static final Type<TardisExteriorSyncPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "tardis_exterior_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, TardisExteriorSyncPayload> STREAM_CODEC = StreamCodec.composite(
            TardisExterior.STREAM_CODEC.apply(ByteBufCodecs.list()),
            TardisExteriorSyncPayload::exteriors,
            ByteBufCodecs.STRING_UTF8,
            TardisExteriorSyncPayload::defaultId,
            TardisExteriorSyncPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

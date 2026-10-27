package com.noahtnt2009.gallifreyan_chronicles.network;

import com.noahtnt2009.gallifreyan_chronicles.tardis.interior.TardisInteriorOptions;
import com.noahtnt2009.gallifreyan_chronicles.tardis.interior.TardisInteriorRegistry;
import com.noahtnt2009.gallifreyan_chronicles.util.GCUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jspecify.annotations.NonNull;

import java.util.List;

public record TardisInteriorSyncPayload(List<TardisInteriorOptions.Option> options,
                                        String defaultId) implements CustomPacketPayload {
    public static final Type<TardisInteriorSyncPayload> TYPE =
            new Type<>(GCUtils.modIdentifier("tardis_interior_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, TardisInteriorSyncPayload> STREAM_CODEC = StreamCodec.composite(
            TardisInteriorOptions.Option.STREAM_CODEC.apply(ByteBufCodecs.list()),
            TardisInteriorSyncPayload::options,
            ByteBufCodecs.STRING_UTF8,
            TardisInteriorSyncPayload::defaultId,
            TardisInteriorSyncPayload::new
    );

    public static TardisInteriorSyncPayload create() {
        return new TardisInteriorSyncPayload(TardisInteriorOptions.fromRegistry(), TardisInteriorRegistry.defaultId());
    }

    public void apply() {
        TardisInteriorOptions.setClient(options, defaultId);
    }

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

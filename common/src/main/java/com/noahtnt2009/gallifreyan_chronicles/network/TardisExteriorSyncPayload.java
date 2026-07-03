package com.noahtnt2009.gallifreyan_chronicles.network;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.TardisExterior;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.List;

/**
 * S2C payload carrying the full TARDIS exterior catalog from the logical server down to the client.
 * <p>
 * Exteriors are loaded server-side only via {@link com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.TardisExteriorLoader}
 * (a {@code SimpleJsonResourceReloadListener}, which only ever runs on the logical server). Without this
 * packet the client-side {@code TardisExteriorRegistry} stays empty and any client-only rendering code that
 * reads from it (block model lookups, GeoRenderState data, etc.) silently falls back to nothing. This payload
 * is sent once on player join and again after every server datapack reload so the client registry mirrors
 * the server's.
 */
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

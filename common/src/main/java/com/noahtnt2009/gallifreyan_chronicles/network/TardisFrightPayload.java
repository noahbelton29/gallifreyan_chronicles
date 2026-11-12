package com.noahtnt2009.gallifreyan_chronicles.network;

import com.noahtnt2009.gallifreyan_chronicles.client.tardis.TardisFrightClient;
import com.noahtnt2009.gallifreyan_chronicles.util.GCUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import org.jspecify.annotations.NonNull;

/* the tardis is afraid of you: warps the console hum and makes the lights stutter for a while */
public record TardisFrightPayload(float severity, int ticks, Identifier dimension) implements CustomPacketPayload {
    public static final Type<TardisFrightPayload> TYPE =
            new Type<>(GCUtils.modIdentifier("tardis_fright"));

    public static final StreamCodec<RegistryFriendlyByteBuf, TardisFrightPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT,
            TardisFrightPayload::severity,
            ByteBufCodecs.VAR_INT,
            TardisFrightPayload::ticks,
            Identifier.STREAM_CODEC,
            TardisFrightPayload::dimension,
            TardisFrightPayload::new
    );

    public void apply() {
        TardisFrightClient.start(severity, ticks, ResourceKey.create(Registries.DIMENSION, dimension));
    }

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

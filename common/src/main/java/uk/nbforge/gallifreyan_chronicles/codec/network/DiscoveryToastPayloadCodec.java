package uk.nbforge.gallifreyan_chronicles.codec.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import uk.nbforge.gallifreyan_chronicles.network.DiscoveryToastPayload;

public final class DiscoveryToastPayloadCodec {
    public static final StreamCodec<RegistryFriendlyByteBuf, DiscoveryToastPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            DiscoveryToastPayload::kind,
            ByteBufCodecs.STRING_UTF8,
            DiscoveryToastPayload::id,
            DiscoveryToastPayload::new
    );

    private DiscoveryToastPayloadCodec() {
    }
}

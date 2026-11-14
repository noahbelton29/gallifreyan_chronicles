package uk.nbforge.gallifreyan_chronicles.codec.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import uk.nbforge.gallifreyan_chronicles.network.ResumeSoundPayload;

public final class ResumeSoundPayloadCodec {
    public static final StreamCodec<RegistryFriendlyByteBuf, ResumeSoundPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.map(Identifier::parse, Identifier::toString),
            ResumeSoundPayload::sound,
            ByteBufCodecs.DOUBLE,
            ResumeSoundPayload::x,
            ByteBufCodecs.DOUBLE,
            ResumeSoundPayload::y,
            ByteBufCodecs.DOUBLE,
            ResumeSoundPayload::z,
            ByteBufCodecs.FLOAT,
            ResumeSoundPayload::volume,
            ByteBufCodecs.FLOAT,
            ResumeSoundPayload::pitch,
            ByteBufCodecs.FLOAT,
            ResumeSoundPayload::offsetSeconds,
            ResumeSoundPayload::new
    );

    private ResumeSoundPayloadCodec() {
    }
}

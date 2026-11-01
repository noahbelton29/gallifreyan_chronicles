package com.noahtnt2009.gallifreyan_chronicles.network;

import com.noahtnt2009.gallifreyan_chronicles.client.GCClientHooks;
import com.noahtnt2009.gallifreyan_chronicles.util.GCUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public record ResumeSoundPayload(Identifier sound, double x, double y, double z, float volume, float pitch,
                                 float offsetSeconds) implements CustomPacketPayload {
    public static final Type<ResumeSoundPayload> TYPE =
            new Type<>(GCUtils.modIdentifier("resume_sound"));

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

    public void apply() {
        GCClientHooks.playResumedSound(this);
    }

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

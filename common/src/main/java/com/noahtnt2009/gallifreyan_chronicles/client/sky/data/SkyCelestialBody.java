package com.noahtnt2009.gallifreyan_chronicles.client.sky.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record SkyCelestialBody(
        BodyType type,
        float yaw,
        float distance,
        float scale,
        boolean useMoonPhase
) {
    public static final Codec<SkyCelestialBody> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            BodyType.CODEC.fieldOf("type").forGetter(SkyCelestialBody::type),
            Codec.FLOAT.optionalFieldOf("yaw", 0.0f).forGetter(SkyCelestialBody::yaw),
            Codec.FLOAT.optionalFieldOf("distance", 0.0f).forGetter(SkyCelestialBody::distance),
            Codec.FLOAT.optionalFieldOf("scale", 1.0f).forGetter(SkyCelestialBody::scale),
            Codec.BOOL.optionalFieldOf("use_moon_phase", true).forGetter(SkyCelestialBody::useMoonPhase)
    ).apply(inst, SkyCelestialBody::new));

    public static final StreamCodec<FriendlyByteBuf, SkyCelestialBody> STREAM_CODEC = StreamCodec.composite(
            BodyType.STREAM_CODEC, SkyCelestialBody::type,
            ByteBufCodecs.FLOAT, SkyCelestialBody::yaw,
            ByteBufCodecs.FLOAT, SkyCelestialBody::distance,
            ByteBufCodecs.FLOAT, SkyCelestialBody::scale,
            ByteBufCodecs.BOOL, SkyCelestialBody::useMoonPhase,
            SkyCelestialBody::new
    );

    public enum BodyType {
        SUN,
        MOON;

        public static final Codec<BodyType> CODEC = Codec.STRING.xmap(
                s -> BodyType.valueOf(s.toUpperCase()),
                b -> b.name().toLowerCase()
        );

        public static final StreamCodec<FriendlyByteBuf, BodyType> STREAM_CODEC =
                StreamCodec.of(
                        FriendlyByteBuf::writeEnum,
                        buf -> buf.readEnum(BodyType.class)
                );
    }
}
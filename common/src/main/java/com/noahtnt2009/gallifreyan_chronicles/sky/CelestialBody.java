package com.noahtnt2009.gallifreyan_chronicles.sky;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.noahtnt2009.gallifreyan_chronicles.data.IdentifiableData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public record CelestialBody(String id, Identifier texture, float size, int color, SkyBody.Blend blend,
                            boolean fadesInRain) implements IdentifiableData<CelestialBody> {
    private static final String UNSET_ID = "__unset__";

    public static final Codec<CelestialBody> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Identifier.CODEC.fieldOf("texture").forGetter(CelestialBody::texture),
            Codec.floatRange(0.0F, 200.0F).optionalFieldOf("size", SkyBody.DEFAULT_SIZE).forGetter(CelestialBody::size),
            HexColor.CODEC.optionalFieldOf("color", 0xFFFFFFFF).forGetter(CelestialBody::color),
            SkyBody.Blend.CODEC.optionalFieldOf("blend", SkyBody.Blend.NORMAL).forGetter(CelestialBody::blend),
            Codec.BOOL.optionalFieldOf("fades_in_rain", true).forGetter(CelestialBody::fadesInRain)
    ).apply(inst, (texture, size, color, blend, fadesInRain) ->
            new CelestialBody(UNSET_ID, texture, size, color, blend, fadesInRain)));

    public static final StreamCodec<FriendlyByteBuf, CelestialBody> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, CelestialBody::id,
            ByteBufCodecs.fromCodec(CODEC), body -> body,
            (id, body) -> body.withId(id)
    );

    @Override
    public CelestialBody withId(String id) {
        return new CelestialBody(id, texture, size, color, blend, fadesInRain);
    }
}

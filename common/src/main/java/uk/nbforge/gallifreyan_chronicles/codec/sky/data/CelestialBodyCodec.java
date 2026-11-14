package uk.nbforge.gallifreyan_chronicles.codec.sky.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import uk.nbforge.gallifreyan_chronicles.codec.HexColorCodec;
import uk.nbforge.gallifreyan_chronicles.sky.data.CelestialBody;
import uk.nbforge.gallifreyan_chronicles.sky.data.SkyBody;

public final class CelestialBodyCodec {
    public static final Codec<CelestialBody> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Identifier.CODEC.fieldOf("texture").forGetter(CelestialBody::texture),
            Codec.floatRange(0.0F, 200.0F).optionalFieldOf("size", SkyBody.DEFAULT_SIZE).forGetter(CelestialBody::size),
            HexColorCodec.CODEC.optionalFieldOf("color", 0xFFFFFFFF).forGetter(CelestialBody::color),
            SkyBodyCodec.BLEND.optionalFieldOf("blend", SkyBody.Blend.NORMAL).forGetter(CelestialBody::blend),
            Codec.BOOL.optionalFieldOf("fades_in_rain", true).forGetter(CelestialBody::fadesInRain)
    ).apply(inst, (texture, size, color, blend, fadesInRain) ->
            new CelestialBody(CelestialBody.UNSET_ID, texture, size, color, blend, fadesInRain)));

    public static final StreamCodec<FriendlyByteBuf, CelestialBody> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, CelestialBody::id,
            ByteBufCodecs.fromCodec(CODEC), body -> body,
            (id, body) -> body.withId(id)
    );

    private CelestialBodyCodec() {
    }
}

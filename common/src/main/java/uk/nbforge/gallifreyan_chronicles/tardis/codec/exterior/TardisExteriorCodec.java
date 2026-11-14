package uk.nbforge.gallifreyan_chronicles.tardis.codec.exterior;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import uk.nbforge.gallifreyan_chronicles.tardis.TardisExterior;

public final class TardisExteriorCodec {
    public static final Codec<TardisExterior> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.STRING.optionalFieldOf("id", "").forGetter(TardisExterior::id)
    ).apply(inst, TardisExterior::new));

    public static final StreamCodec<FriendlyByteBuf, TardisExterior> STREAM_CODEC = StreamCodec.of(
            (buf, exterior) -> ByteBufCodecs.STRING_UTF8.encode(buf, exterior.id()),
            buf -> new TardisExterior(ByteBufCodecs.STRING_UTF8.decode(buf))
    );

    private TardisExteriorCodec() {
    }
}

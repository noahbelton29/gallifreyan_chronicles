package uk.nbforge.gallifreyan_chronicles.tardis.codec.mind.data;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.data.MindRange;

public final class MindRangeCodec {
    private static final Codec<MindRange> MIN_MAX = RecordCodecBuilder.create(inst -> inst.group(
            Codec.FLOAT.fieldOf("min").forGetter(MindRange::min),
            Codec.FLOAT.fieldOf("max").forGetter(MindRange::max)
    ).apply(inst, MindRange::new));

    public static final Codec<MindRange> CODEC = Codec.either(Codec.FLOAT, MIN_MAX).xmap(
            either -> either.map(MindRange::of, range -> range),
            range -> range.min() == range.max() ? Either.left(range.min()) : Either.right(range));

    private MindRangeCodec() {
    }
}

package uk.nbforge.gallifreyan_chronicles.tardis.codec.interior;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import uk.nbforge.gallifreyan_chronicles.tardis.TardisInterior;

public final class TardisInteriorCodec {
    public static final Codec<TardisInterior> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.STRING.optionalFieldOf("id", "").forGetter(TardisInterior::id),
            Codec.STRING.optionalFieldOf("exterior").forGetter(TardisInterior::exterior)
    ).apply(inst, TardisInterior::new));

    private TardisInteriorCodec() {
    }
}

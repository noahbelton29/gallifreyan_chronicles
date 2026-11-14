package uk.nbforge.gallifreyan_chronicles.tardis.codec.mind;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.TardisBond;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.TardisMind;

public final class TardisBondCodec {
    public static final Codec<TardisBond> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.FLOAT.optionalFieldOf("trust", TardisMind.START_TRUST).forGetter(TardisBond::trust),
            Codec.FLOAT.optionalFieldOf("fear", 0f).forGetter(TardisBond::fear),
            Codec.LONG.optionalFieldOf("fear_tick", 0L).forGetter(TardisBond::fearTick),
            Codec.INT.optionalFieldOf("hits", 0).forGetter(TardisBond::hits),
            Codec.BOOL.optionalFieldOf("betrayed", false).forGetter(TardisBond::betrayed),
            Codec.FLOAT.optionalFieldOf("trauma", 0f).forGetter(TardisBond::trauma),
            Codec.LONG.optionalFieldOf("trauma_tick", 0L).forGetter(TardisBond::traumaTick)
    ).apply(inst, TardisBond::new));

    private TardisBondCodec() {
    }
}

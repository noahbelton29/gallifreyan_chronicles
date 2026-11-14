package uk.nbforge.gallifreyan_chronicles.tardis.codec.mind;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import net.minecraft.core.UUIDUtil;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.TardisBond;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.TardisMind;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.data.MindIds;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.disability.TardisDisability;
import uk.nbforge.gallifreyan_chronicles.tardis.codec.mind.data.MindIdsCodec;
import uk.nbforge.gallifreyan_chronicles.tardis.codec.mind.disability.TardisDisabilityCodec;

public final class TardisMindCodec {
    /**
     * Codec for loading and saving a TARDIS's mental state.
     */
    public static final Codec<TardisMind> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            MindIdsCodec.CODEC.fieldOf("personality").forGetter(TardisMind::personalityId),
            MindIdsCodec.CODEC.listOf().optionalFieldOf("traits", List.of()).forGetter(TardisMind::traitIds),
            Codec.FLOAT.optionalFieldOf("stress", 0f).forGetter(TardisMind::stress),
            Codec.FLOAT.optionalFieldOf("trust", TardisMind.START_TRUST).forGetter(TardisMind::ownerStartTrust),
            Codec.LONG.optionalFieldOf("stress_tick", 0L).forGetter(TardisMind::stressTick),
            Codec.LONG.optionalFieldOf("last_land", TardisMind.NEVER).forGetter(TardisMind::lastLand),
            Codec.unboundedMap(UUIDUtil.STRING_CODEC, TardisBondCodec.CODEC).optionalFieldOf("bonds", Map.of()).forGetter(TardisMind::bonds),
            TardisDisabilityCodec.CODEC.listOf().optionalFieldOf("disabilities", List.of()).forGetter(TardisMind::disabilities),
            Codec.FLOAT.optionalFieldOf("skill", 0f).forGetter(TardisMind::skill),
            Codec.STRING.listOf().optionalFieldOf("discovered", List.of()).forGetter(TardisMind::discovered),
            Codec.FLOAT.optionalFieldOf("trauma", 0f).forGetter(TardisMind::trauma),
            Codec.LONG.optionalFieldOf("trauma_tick", 0L).forGetter(TardisMind::traumaTick),
            Codec.FLOAT.optionalFieldOf("recovery", 0f).forGetter(TardisMind::recovery)
    ).apply(inst, TardisMind::new));

    private TardisMindCodec() {
    }
}

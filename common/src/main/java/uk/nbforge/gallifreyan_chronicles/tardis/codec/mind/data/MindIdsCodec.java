package uk.nbforge.gallifreyan_chronicles.tardis.codec.mind.data;

import com.mojang.serialization.Codec;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.data.MindIds;

public final class MindIdsCodec {
    public static final Codec<String> CODEC = Codec.STRING.xmap(MindIds::normalize, id -> id);

    private MindIdsCodec() {
    }
}

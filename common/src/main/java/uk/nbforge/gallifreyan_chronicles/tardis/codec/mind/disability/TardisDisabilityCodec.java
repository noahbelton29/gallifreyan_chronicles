package uk.nbforge.gallifreyan_chronicles.tardis.codec.mind.disability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.disability.TardisDisability;

public final class TardisDisabilityCodec {
    /**
     * Handles saving and loading disabilities using their IDs.
     * Returns an error if the saved ID doesn't match a known disability.
     */
    public static final Codec<TardisDisability> CODEC = Codec.STRING.comapFlatMap(
            id -> TardisDisability.byId(id).map(DataResult::success)
                    .orElseGet(() -> DataResult.error(() -> "unknown tardis disability " + id)),
            TardisDisability::id);

    private TardisDisabilityCodec() {
    }
}

package uk.nbforge.gallifreyan_chronicles.tardis.mind.disability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Arrays;
import java.util.Optional;

public enum TardisDisability {
    DYSLEXIC("dyslexic", 0.15f),

    /** a tardis isnt born with PTSD, it only develops thru abuse */
    PTSD("ptsd", 0f);

    public static final Codec<TardisDisability> CODEC = Codec.STRING.comapFlatMap(
            id -> TardisDisability.byId(id)
                    .map(DataResult::success)
                    .orElseGet(() -> DataResult.error(() -> "unknown tardis disability " + id)),
            TardisDisability::id);

    private final String id;
    private final float chance;

    TardisDisability(String id, float chance) {
        this.id = id;
        this.chance = chance;
    }

    public String id() {
        return id;
    }

    public float chance() {
        return chance;
    }

    public static Optional<TardisDisability> byId(String id) {
        return Arrays.stream(values()).filter(d -> d.id.equals(id)).findFirst();
    }
}

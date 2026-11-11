package com.noahtnt2009.gallifreyan_chronicles.tardis.mind;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import java.util.Arrays;
import java.util.Optional;

public enum TardisPersonality {
    OBEDIENT("obedient", 35, 101f, -1f, 0f),
    SUBMISSIVE("submissive", 30, 60f, 20f, 25f),
    DEFENSIVE("defensive", 25, 50f, 30f, 45f),
    AGGRESSIVE("aggressive", 10, 40f, 35f, 75f);

    public static final Codec<TardisPersonality> CODEC = Codec.STRING.comapFlatMap(
            id -> byId(id).map(DataResult::success)
                    .orElseGet(() -> DataResult.error(() -> "unknown tardis personality " + id)),
            TardisPersonality::id);

    private final String id;
    private final int weight;
    private final float stressLimit;
    private final float trustLimit;
    private final float fearToComply;

    TardisPersonality(String id, int weight, float stressLimit, float trustLimit, float fearToComply) {
        this.id = id;
        this.weight = weight;
        this.stressLimit = stressLimit;
        this.trustLimit = trustLimit;
        this.fearToComply = fearToComply;
    }

    public float fearToComply() {
        return fearToComply;
    }

    public String id() {
        return id;
    }

    public int weight() {
        return weight;
    }

    public float stressLimit() {
        return stressLimit;
    }

    public float trustLimit() {
        return trustLimit;
    }

    public static Optional<TardisPersonality> byId(String id) {
        return Arrays.stream(values()).filter(p -> p.id.equals(id)).findFirst();
    }
}

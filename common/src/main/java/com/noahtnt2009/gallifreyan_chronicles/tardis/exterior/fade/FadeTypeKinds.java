package com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.fade;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public final class FadeTypeKinds {
    private static final Map<String, MapCodec<? extends FadeType>> KINDS = new LinkedHashMap<>();
    private static final Map<MapCodec<? extends FadeType>, String> IDS = new LinkedHashMap<>();

    private static final Codec<MapCodec<? extends FadeType>> KIND_CODEC = Codec.STRING.comapFlatMap(
            id -> {
                MapCodec<? extends FadeType> codec = KINDS.get(id);
                return codec != null
                        ? DataResult.<MapCodec<? extends FadeType>>success(codec)
                        : DataResult.<MapCodec<? extends FadeType>>error(() -> "Unknown fade type: " + id
                                + " (available: " + String.join(", ", KINDS.keySet()) + ")");
            },
            codec -> {
                String id = IDS.get(codec);
                if (id == null) {
                    throw new IllegalStateException("Fade type is not registered: " + codec);
                }
                return id;
            }
    );

    public static final Codec<FadeType> CODEC = KIND_CODEC.dispatch("type", FadeType::codec, Function.identity());

    static {
        register("pulse", PulseFadeType.CODEC);
        register("fade", LinearFadeType.CODEC);
    }

    private FadeTypeKinds() {
    }

    public static void register(String id, MapCodec<? extends FadeType> codec) {
        if (KINDS.putIfAbsent(id, codec) != null) {
            throw new IllegalArgumentException("Duplicate fade type: " + id);
        }
        IDS.put(codec, id);
    }

    public static Set<String> allIds() {
        return Collections.unmodifiableSet(KINDS.keySet());
    }
}

package com.noahtnt2009.gallifreyan_chronicles.tardis.control;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public final class TardisControlTypes {
    private static final Map<String, MapCodec<? extends TardisControl>> TYPES = new LinkedHashMap<>();
    private static final Map<MapCodec<? extends TardisControl>, String> IDS = new LinkedHashMap<>();

    private static final Codec<MapCodec<? extends TardisControl>> TYPE_CODEC = Codec.STRING.comapFlatMap(
            id -> {
                MapCodec<? extends TardisControl> codec = TYPES.get(id);
                return codec != null
                        ? DataResult.<MapCodec<? extends TardisControl>>success(codec)
                        : DataResult.<MapCodec<? extends TardisControl>>error(() -> "Unknown TARDIS control type: " + id);
            },
            codec -> {
                String id = IDS.get(codec);
                if (id == null) {
                    throw new IllegalStateException("TARDIS control type is not registered: " + codec);
                }
                return id;
            }
    );

    public static final Codec<TardisControl> CODEC = TYPE_CODEC.dispatch("type", TardisControl::codec, Function.identity());

    private TardisControlTypes() {
    }

    public static void register(String id, MapCodec<? extends TardisControl> codec) {
        if (TYPES.putIfAbsent(id, codec) != null) {
            throw new IllegalArgumentException("Duplicate TARDIS control type: " + id);
        }
        IDS.put(codec, id);
    }

    public static boolean contains(String id) {
        return TYPES.containsKey(id);
    }

    public static Set<String> allIds() {
        return Collections.unmodifiableSet(TYPES.keySet());
    }
}

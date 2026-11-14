package uk.nbforge.gallifreyan_chronicles.tardis.control;

import com.mojang.serialization.MapCodec;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class TardisControlTypes {
    private static final Map<String, MapCodec<? extends TardisControl>> TYPES = new LinkedHashMap<>();
    private static final Map<MapCodec<? extends TardisControl>, String> IDS = new LinkedHashMap<>();
    private static final Map<Class<?>, MapCodec<? extends TardisControl>> BY_CLASS = new HashMap<>();

    private TardisControlTypes() {
    }

    public static void register(String id, MapCodec<? extends TardisControl> codec) {
        if (TYPES.putIfAbsent(id, codec) != null) {
            throw new IllegalArgumentException("Duplicate TARDIS control type: " + id);
        }
        IDS.put(codec, id);
    }

    public static @Nullable MapCodec<? extends TardisControl> byId(String id) {
        return TYPES.get(id);
    }

    public static @Nullable String idOf(MapCodec<? extends TardisControl> codec) {
        return IDS.get(codec);
    }

    /* registers a control type and remembers its codec by class, so the control can find it again from itself */
    public static void register(String id, Class<? extends TardisControl> type, MapCodec<? extends TardisControl> codec) {
        register(id, codec);
        BY_CLASS.put(type, codec);
    }

    /* the codec a control was registered with, found from its class or the nearest registered parent */
    public static MapCodec<? extends TardisControl> codecFor(TardisControl control) {
        for (Class<?> type = control.getClass(); type != null; type = type.getSuperclass()) {
            MapCodec<? extends TardisControl> codec = BY_CLASS.get(type);
            if (codec != null) return codec;
        }
        throw new IllegalStateException("TARDIS control type is not registered: " + control.getClass().getName());
    }

    public static boolean contains(String id) {
        return TYPES.containsKey(id);
    }

    public static Set<String> allIds() {
        return Collections.unmodifiableSet(TYPES.keySet());
    }
}

package uk.nbforge.gallifreyan_chronicles.tardis.control;

import com.mojang.serialization.MapCodec;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.jspecify.annotations.Nullable;
import uk.nbforge.gallifreyan_chronicles.Constants;

/** a type can exist without a control, "noop" is only a type, and an unknown control id gives the noop control */
public final class TardisControls {
    private static final Map<String, MapCodec<? extends TardisControl>> TYPES = new LinkedHashMap<>();
    private static final Map<MapCodec<? extends TardisControl>, String> TYPE_IDS = new LinkedHashMap<>();
    private static final Map<Class<?>, MapCodec<? extends TardisControl>> TYPES_BY_CLASS = new HashMap<>();
    private static final Map<String, TardisControl> CONTROLS = new LinkedHashMap<>();

    private TardisControls() {}

    public static void register(String id, MapCodec<? extends TardisControl> codec, TardisControl control) {
        registerType(id, codec);
        registerControl(id, control);
    }

    /** also remembers the codec by the control's class so {@link #codecFor} can find it from the control */
    public static void register(
            String id, Class<?> type, MapCodec<? extends TardisControl> codec, TardisControl control) {
        registerType(id, type, codec);
        registerControl(id, control);
    }

    public static void registerType(String id, MapCodec<? extends TardisControl> codec) {
        if (TYPES.putIfAbsent(id, codec) != null) {
            throw new IllegalArgumentException("Duplicate TARDIS control type: " + id);
        }
        TYPE_IDS.put(codec, id);
    }

    public static void registerType(String id, Class<?> type, MapCodec<? extends TardisControl> codec) {
        registerType(id, codec);
        TYPES_BY_CLASS.put(type, codec);
    }

    private static void registerControl(String id, TardisControl control) {
        if (CONTROLS.putIfAbsent(id, control) != null) {
            throw new IllegalArgumentException("Duplicate TARDIS control: " + id);
        }
    }

    public static TardisControl get(String id) {
        return CONTROLS.getOrDefault(id, NoopControl.INSTANCE);
    }

    public static boolean isRegistered(String id) {
        return CONTROLS.containsKey(id);
    }

    public static Set<String> ids() {
        return Collections.unmodifiableSet(CONTROLS.keySet());
    }

    public static Map<String, TardisControl> all() {
        return Collections.unmodifiableMap(CONTROLS);
    }

    public static @Nullable MapCodec<? extends TardisControl> typeCodec(String id) {
        return TYPES.get(id);
    }

    public static @Nullable String typeId(MapCodec<? extends TardisControl> codec) {
        return TYPE_IDS.get(codec);
    }

    public static boolean hasType(String id) {
        return TYPES.containsKey(id);
    }

    public static Set<String> typeIds() {
        return Collections.unmodifiableSet(TYPES.keySet());
    }

    public static MapCodec<? extends TardisControl> codecFor(TardisControl control) {
        for (Class<?> type = control.getClass(); type != null; type = type.getSuperclass()) {
            MapCodec<? extends TardisControl> codec = TYPES_BY_CLASS.get(type);
            if (codec != null) return codec;
        }
        throw new IllegalStateException(
                "TARDIS control type is not registered: " + control.getClass().getName());
    }

    public static String translationKey(String id) {
        return "control." + Constants.MOD_ID + "." + id;
    }
}

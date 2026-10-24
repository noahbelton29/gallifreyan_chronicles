package com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.fade;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class FadeTypes {
    public static final String NEW_WHO_ID = Constants.MOD_ID + ":newwho";
    public static final String CLASSIC_WHO_ID = Constants.MOD_ID + ":classicwho";

    /** Modern series fade with the pulse. Used for new TARDISes and when a saved id is missing or unknown. */
    public static final FadeType NEW_WHO =
            new PulseFadeType(NEW_WHO_ID, FadeTiming.DEFAULT, 30.0f, 2.5f, 1.0f);
    /** Classic series fade, no pulse. */
    public static final FadeType CLASSIC_WHO =
            new LinearFadeType(CLASSIC_WHO_ID, FadeTiming.DEFAULT, 1.0f);
    public static final FadeType DEFAULT = NEW_WHO;

    private static final Map<String, FadeType> BUILT_IN = new LinkedHashMap<>();
    private static final Map<String, FadeType> LOADED = new LinkedHashMap<>();
    private static volatile int generation = 0;

    static {
        BUILT_IN.put(NEW_WHO_ID, NEW_WHO);
        BUILT_IN.put(CLASSIC_WHO_ID, CLASSIC_WHO);
    }

    private FadeTypes() {
    }

    public static String normalize(String id) {
        String lowered = id.toLowerCase(Locale.ROOT);
        return lowered.contains(":") ? lowered : Constants.MOD_ID + ":" + lowered;
    }

    public static void clear() {
        LOADED.clear();
    }

    public static int generation() {
        return generation;
    }

    public static void markReloaded() {
        generation++;
    }

    public static FadeType register(FadeType type) {
        LOADED.put(type.id(), type);
        return type;
    }

    public static int loadedSize() {
        return LOADED.size();
    }

    public static int size() {
        return getAll().size();
    }

    public static boolean contains(@Nullable String id) {
        if (id == null) return false;
        String key = normalize(id);
        return LOADED.containsKey(key) || BUILT_IN.containsKey(key);
    }

    public static FadeType get(@Nullable String id) {
        if (id == null) return getDefault();
        String key = normalize(id);
        FadeType loaded = LOADED.get(key);
        if (loaded != null) return loaded;
        return BUILT_IN.getOrDefault(key, getDefault());
    }

    public static FadeType getDefault() {
        FadeType loaded = LOADED.get(NEW_WHO_ID);
        return loaded != null ? loaded : NEW_WHO;
    }

    public static List<String> suggestionIds() {
        String prefix = Constants.MOD_ID + ":";
        return getAll().stream()
                .map(FadeType::id)
                .map(id -> id.startsWith(prefix) ? id.substring(prefix.length()) : id)
                .toList();
    }

    public static Collection<FadeType> getAll() {
        Map<String, FadeType> all = new LinkedHashMap<>(BUILT_IN);
        all.putAll(LOADED);
        return Collections.unmodifiableCollection(all.values());
    }
}

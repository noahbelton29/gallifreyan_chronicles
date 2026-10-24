package com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.fade;

import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public final class FadeTypes {
    private static final Map<String, FadeType> TYPES = new LinkedHashMap<>();

    public static final FadeType NEW_WHO = register(new PulseFadeType());
    public static final FadeType CLASSIC_WHO = register(new ClassicFadeType());

    public static final FadeType DEFAULT = NEW_WHO;

    private FadeTypes() {}

    public static <T extends FadeType> T register(T type) {
        if (TYPES.putIfAbsent(type.id(), type) != null) {
            throw new IllegalArgumentException("Duplicate fade type id: " + type.id());
        }
        return type;
    }

    public static boolean contains(@Nullable String id) {
        return id != null && TYPES.containsKey(id.toLowerCase(Locale.ROOT));
    }

    public static FadeType get(@Nullable String id) {
        if (id == null) return DEFAULT;
        return TYPES.getOrDefault(id.toLowerCase(Locale.ROOT), DEFAULT);
    }

    public static Collection<FadeType> getAll() {
        return Collections.unmodifiableCollection(TYPES.values());
    }
}

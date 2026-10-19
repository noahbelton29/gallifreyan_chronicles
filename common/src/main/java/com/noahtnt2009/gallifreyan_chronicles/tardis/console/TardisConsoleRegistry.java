package com.noahtnt2009.gallifreyan_chronicles.tardis.console;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.data.GCJsonDataRegistry;
import com.noahtnt2009.gallifreyan_chronicles.util.GCUtils;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

public class TardisConsoleRegistry {
    public static final String DEFAULT_ID = GCUtils.ofNamespace("brachacki_console");

    private static final GCJsonDataRegistry<TardisConsole> REGISTRY = new GCJsonDataRegistry<>();
    private static TardisConsole defaultConsole;
    private static final Map<String, TardisConsoleVariant> VARIANTS = new ConcurrentSkipListMap<>();

    private TardisConsoleRegistry() {
    }

    public static void clear() {
        REGISTRY.clear();
        defaultConsole = null;
    }

    public static TardisConsole register(TardisConsole console) {
        return REGISTRY.register(console);
    }

    public static void setDefault(TardisConsole console) {
        defaultConsole = console;
    }

    public static TardisConsole get(String id) {
        TardisConsole result = REGISTRY.getById(id);
        if (result != null) return result;
        if (defaultConsole != null) return defaultConsole;
        return REGISTRY.getAll().iterator().next();
    }

    public static boolean contains(String id) {
        return REGISTRY.contains(id);
    }
    public static Collection<TardisConsole> getAll() {
        return REGISTRY.getAll();
    }
    public static TardisConsole getDefault() {
        return defaultConsole;
    }
    public static int size() {
        return REGISTRY.size();
    }

    public static void clearVariants() {
        VARIANTS.clear();
    }

    public static void registerVariant(TardisConsoleVariant variant) {
        VARIANTS.put(variant.localId(), variant);
    }

    public static @Nullable TardisConsoleVariant getVariant(String localId) {
        return VARIANTS.get(localId);
    }

    public static Collection<TardisConsoleVariant> getAllVariants() {
        return VARIANTS.values();
    }

    public static List<TardisConsoleVariant> variantsOf(String consoleId) {
        return VARIANTS.values().stream()
                .filter(variant -> variant.parent().equals(consoleId))
                .toList();
    }

    public static int variantSize() {
        return VARIANTS.size();
    }

    public static void validateVariants() {
        if (REGISTRY.size() == 0 || VARIANTS.isEmpty()) return;

        for (TardisConsoleVariant variant : VARIANTS.values()) {
            TardisConsole parent = REGISTRY.getById(variant.parent());
            if (parent == null) {
                Constants.LOG.warn("Console variant '{}' has unknown parent '{}'; it will not be usable",
                        variant.id(), variant.parent());
                continue;
            }

            for (String group : variant.glowTextures().keySet()) {
                boolean known = parent.glowGroups().stream().anyMatch(g -> g.name().equals(group));
                if (!known) {
                    Constants.LOG.warn("Console variant '{}' overrides glow group '{}' which '{}' does not define",
                            variant.id(), group, parent.id());
                }
            }
        }
    }
}

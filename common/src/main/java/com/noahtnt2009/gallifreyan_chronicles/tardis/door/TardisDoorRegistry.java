package com.noahtnt2009.gallifreyan_chronicles.tardis.door;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

public final class TardisDoorRegistry {
    private static final Map<String, TardisDoorVariant> VARIANTS = new ConcurrentSkipListMap<>();

    private TardisDoorRegistry() {
    }

    public static String normalize(String modelId) {
        return modelId.contains(":") ? modelId : Constants.MOD_ID + ":" + modelId;
    }

    public static void clear() {
        VARIANTS.clear();
    }

    public static void register(TardisDoorVariant variant) {
        VARIANTS.put(variant.localId(), variant);
    }

    public static Collection<TardisDoorVariant> getAll() {
        return VARIANTS.values();
    }

    public static int size() {
        return VARIANTS.size();
    }

    public static List<TardisDoorVariant> variantsOf(String modelId) {
        String parent = normalize(modelId);
        return VARIANTS.values().stream()
                .filter(variant -> normalize(variant.parent()).equals(parent))
                .toList();
    }

    public static List<String> variantIds(String modelId) {
        return variantsOf(modelId).stream().map(TardisDoorVariant::localId).toList();
    }

    private static @Nullable TardisDoorVariant exact(String modelId, String variantId) {
        TardisDoorVariant variant = VARIANTS.get(variantId);
        return variant != null && normalize(variant.parent()).equals(normalize(modelId)) ? variant : null;
    }

    public static boolean hasVariant(String modelId, String variantId) {
        return exact(modelId, variantId) != null;
    }

    public static String defaultVariantId(String modelId) {
        List<TardisDoorVariant> all = variantsOf(modelId);
        for (TardisDoorVariant variant : all) {
            if (variant.isDefault()) return variant.localId();
        }
        return all.isEmpty() ? "" : all.getFirst().localId();
    }

    public static @Nullable TardisDoorVariant resolve(String modelId, String variantId) {
        TardisDoorVariant variant = exact(modelId, variantId);
        return variant != null ? variant : exact(modelId, defaultVariantId(modelId));
    }

    public static String textureIdFor(String modelId, String variantId) {
        TardisDoorVariant variant = resolve(modelId, variantId);
        return variant != null ? variant.texture() : normalize(modelId);
    }

    public static void validate() {
        for (TardisDoorVariant variant : VARIANTS.values()) {
            long defaults = variantsOf(variant.parent()).stream().filter(TardisDoorVariant::isDefault).count();
            if (defaults > 1) {
                Constants.LOG.warn("Door model '{}' has {} variants flagged as default; using the first alphabetically",
                        variant.parent(), defaults);
            }
        }
    }
}

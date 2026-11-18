package uk.nbforge.gallifreyan_chronicles.tardis.door;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.platform.Services;
import uk.nbforge.gallifreyan_chronicles.util.GCUtil;

/**
 * door blocks register before datapacks exist, so this reads the door variant jsons straight out of our jar. same
 * folder, ids and codec as the reload listener so the two never disagree about what a variant is called
 */
public final class DoorBlockJson {
    private static final String DATA_ROOT = "data";
    private static final FileToIdConverter FILES = FileToIdConverter.json(TardisDoorVariant.FOLDER);
    private static final String DEFAULT_PARTICLE = Constants.MOD_ID + ":brachacki_wall_block";

    public record Entry(String block, String variant, String model, int priority, String particle) {}

    private static @Nullable List<TardisDoorVariant> variants;
    private static @Nullable List<Entry> entries;

    private DoorBlockJson() {}

    public static List<Entry> entries() {
        if (entries != null) return entries;
        List<Entry> found = new ArrayList<>();
        for (TardisDoorVariant v : bundledVariants()) {
            if (v.interior() || v.block().isEmpty()) continue;
            String block = v.block().contains(":") ? v.block() : Constants.MOD_ID + ":" + v.block();
            if (!block.startsWith(Constants.MOD_ID + ":")) {
                Constants.LOG.warn(
                        "[GC] door variant {} has block {} outside {}, skipped", v.id(), block, Constants.MOD_ID);
                continue;
            }
            String particle = v.particle().isEmpty() ? DEFAULT_PARTICLE : v.particle();
            found.add(new Entry(local(block), v.localId(), local(v.parent()), v.priority(), particle));
        }
        return entries = List.copyOf(found);
    }

    public static List<TardisDoorVariant> bundledVariants() {
        if (variants != null) return variants;
        List<TardisDoorVariant> found = new ArrayList<>();
        try {
            Services.PLATFORM
                    .readModFiles(Constants.MOD_ID, DATA_ROOT, path -> variantId(path) != null)
                    .forEach((path, bytes) -> parse(variantId(path), bytes, found));
        } catch (RuntimeException e) {
            Constants.LOG.error("[GC] couldn't read the door variant jsons", e);
        }
        if (found.isEmpty()) Constants.LOG.warn("[GC] no door variants found, no door blocks registered");
        found.sort(Comparator.comparingInt(TardisDoorVariant::priority).thenComparing(TardisDoorVariant::localId));
        return variants = List.copyOf(found);
    }

    /** data/<namespace>/tardis_door_variant/<path>.json -> namespace:path, same as the reload listener names them */
    private static @Nullable Identifier variantId(String jarPath) {
        String prefix = DATA_ROOT + "/";
        if (!jarPath.startsWith(prefix)) return null;
        int slash = jarPath.indexOf('/', prefix.length());
        if (slash < 0) return null;
        Identifier file = Identifier.tryBuild(jarPath.substring(prefix.length(), slash), jarPath.substring(slash + 1));
        if (file == null
                || !file.getPath().startsWith(TardisDoorVariant.FOLDER + "/")
                || !file.getPath().endsWith(".json")) return null;
        return FILES.fileToId(file);
    }

    private static void parse(@Nullable Identifier id, byte[] bytes, List<TardisDoorVariant> out) {
        if (id == null) return;
        try {
            TardisDoorVariant.CODEC
                    .parse(JsonOps.INSTANCE, JsonParser.parseString(new String(bytes, StandardCharsets.UTF_8)))
                    .resultOrPartial(error -> Constants.LOG.error("[GC] bad door variant {}: {}", id, error))
                    .ifPresent(v -> out.add(v.withId(id.toString())));
        } catch (RuntimeException e) {
            Constants.LOG.error("[GC] bad door variant {}", id, e);
        }
    }

    private static String local(String id) {
        return GCUtil.localPath(id);
    }
}

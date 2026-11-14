package uk.nbforge.gallifreyan_chronicles.datagen.util;

import com.google.gson.JsonElement;
import uk.nbforge.gallifreyan_chronicles.util.GCUtil;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/** Base for providers that write one data pack JSON per entry into a mod data folder. */
public abstract class GCJsonDataProvider implements DataProvider {
    private final PackOutput.PathProvider pathProvider;
    private final String label;
    private final Map<String, JsonElement> entries = new LinkedHashMap<>();

    protected GCJsonDataProvider(PackOutput output, String folder, String label) {
        this.pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, folder);
        this.label = label;
    }

    protected abstract void addEntries();

    /** "tuat_doors" becomes "gallifreyan_chronicles:tuat_doors"; ids that already have a namespace are kept. */
    protected static String mod(String id) {
        return id.contains(":") ? id : uk.nbforge.gallifreyan_chronicles.Constants.MOD_ID + ":" + id;
    }

    protected final void add(String name, JsonElement json) {
        if (entries.put(name, json) != null) throw new IllegalStateException("Duplicate " + label + " entry " + name);
    }

    @Override
    public @NonNull CompletableFuture<?> run(@NonNull CachedOutput cache) {
        entries.clear();
        addEntries();
        List<CompletableFuture<?>> futures = new ArrayList<>();
        entries.forEach((name, json) ->
                futures.add(DataProvider.saveStable(cache, json, pathProvider.json(GCUtil.modIdentifier(name)))));
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    @Override
    public @NonNull String getName() {
        return "Gallifreyan Chronicles " + label + " Provider";
    }
}

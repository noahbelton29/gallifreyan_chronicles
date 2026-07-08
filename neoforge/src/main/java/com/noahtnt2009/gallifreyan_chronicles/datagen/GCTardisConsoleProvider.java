package com.noahtnt2009.gallifreyan_chronicles.datagen;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.tardis.console.TardisConsole;
import com.noahtnt2009.gallifreyan_chronicles.util.GCUtils;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class GCTardisConsoleProvider implements DataProvider {
    private final PackOutput.PathProvider pathProvider;
    private final List<TardisConsole> consoles = new ArrayList<>();

    public GCTardisConsoleProvider(PackOutput output) {
        this.pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "tardis_console");
        registerDefaults();
    }

    private void registerDefaults() {
        addConsole("first_doctors_console");
    }

    private void addConsole(String path) {
        consoles.add(new TardisConsole(GCUtils.ofNamespace(path)));
    }

    @Override
    public @NonNull CompletableFuture<?> run(@NonNull CachedOutput cache) {
        List<CompletableFuture<?>> futures = new ArrayList<>();

        for (TardisConsole console : consoles) {
            String filePath = console.id().contains(":")
                    ? console.id().substring(console.id().indexOf(':') + 1)
                    : console.id();

            Identifier fileId = GCUtils.of(filePath);

            var encodeResult = TardisConsole.CODEC.encodeStart(JsonOps.INSTANCE, console);
            encodeResult.ifSuccess(json -> {
                futures.add(DataProvider.saveStable(cache, (JsonElement) json, pathProvider.json(fileId)));
            });
            encodeResult.ifError(err ->
                    Constants.LOG.error("Failed to encode TARDIS console {}: {}", console.id(), err.message())
            );
        }

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    @Override
    public @NonNull String getName() {
        return "Gallifreyan Chronicles TARDIS Console Provider";
    }
}

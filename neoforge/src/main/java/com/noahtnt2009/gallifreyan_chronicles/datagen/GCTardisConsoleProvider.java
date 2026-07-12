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
import net.minecraft.world.phys.Vec3;
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
        addConsole("first_doctors_console", List.of(
                new TardisConsole.ControlEntry(
                        "flight_lever",
                        new Vec3(0.3106, 0.5195, -0.9609),
                        0.125f,
                        0.25f,
                        0.225f
                ),
                new TardisConsole.ControlEntry(
                        "handbrake",
                        new Vec3(0.0625, 0.5195, -0.9609),
                        0.125f,
                        0.25f,
                        0.225f
                )
        ));
    }

    private void addConsole(String path, List<TardisConsole.ControlEntry> controls) {
        consoles.add(new TardisConsole(GCUtils.ofNamespace(path), controls));
    }

    private void addConsole(String path) {
        addConsole(path, List.of());
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
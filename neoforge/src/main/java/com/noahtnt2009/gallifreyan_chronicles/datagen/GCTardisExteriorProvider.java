package com.noahtnt2009.gallifreyan_chronicles.datagen;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.TardisExterior;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class GCTardisExteriorProvider implements DataProvider {
    private final PackOutput.PathProvider pathProvider;
    private final List<TardisExterior> exteriors = new ArrayList<>();

    public GCTardisExteriorProvider(PackOutput output) {
        this.pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "tardis_exteriors");
        registerDefaults();
    }

    private void registerDefaults() {
        addExterior("first_doctors_exterior",
                id("first_doctors_exterior"),
                id("textures/block/first_doctors_exterior.png"),
                id("tardis_door/first_doctors_exterior"));

        addExterior("1966_police_box",
                id("1966_police_box"),
                id("textures/block/1966_police_box.png"),
                id("tardis_door/1966_police_box"));

        addExterior("corrupted",
                id("corrupted"),
                id("textures/block/corrupted.png"),
                id("tardis_door/corrupted"));
    }

    private void addExterior(String path, Identifier model, Identifier texture, Identifier animation) {
        String qualifiedId = Constants.MOD_ID + ":" + path;
        exteriors.add(new TardisExterior(qualifiedId, model, texture, animation));
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(Constants.MOD_ID, path);
    }

    @Override
    public @NonNull CompletableFuture<?> run(@NonNull CachedOutput cache) {
        List<CompletableFuture<?>> futures = new ArrayList<>();

        for (TardisExterior exterior : exteriors) {
            String filePath = exterior.id().contains(":")
                    ? exterior.id().substring(exterior.id().indexOf(':') + 1)
                    : exterior.id();

            Identifier fileId = Identifier.fromNamespaceAndPath(Constants.MOD_ID, filePath);

            var encodeResult = TardisExterior.CODEC.encodeStart(JsonOps.INSTANCE, exterior);
            encodeResult.ifSuccess(json -> {
                futures.add(DataProvider.saveStable(cache, (JsonElement) json, pathProvider.json(fileId)));
            });
            encodeResult.ifError(err ->
                    Constants.LOG.error("Failed to encode TARDIS exterior {}: {}", exterior.id(), err.message())
            );
        }

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    @Override
    public @NonNull String getName() {
        return "Gallifreyan Chronicles TARDIS Exterior Provider";
    }
}

package com.noahtnt2009.gallifreyan_chronicles.datagen;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.client.sky.data.DimensionSky;
import com.noahtnt2009.gallifreyan_chronicles.client.sky.data.SkyCelestialBody;
import com.noahtnt2009.gallifreyan_chronicles.init.GCDimensions;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class GCDimensionSkyProvider implements DataProvider {
    private final PackOutput.PathProvider pathProvider;
    private final List<DimensionSky> skies = new ArrayList<>();

    public GCDimensionSkyProvider(PackOutput output) {
        this.pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "dimension_sky");
        registerDefaults();
    }

    private void registerDefaults() {
        addSky("gallifrey",
                GCDimensions.GALLIFREY_LEVEL_KEY.identifier(),
                false,
                List.of(
                        new SkyCelestialBody(SkyCelestialBody.BodyType.SUN, 0f, 0f, 1.0f, true),
                        new SkyCelestialBody(SkyCelestialBody.BodyType.SUN, 25.0f, 45.0f, 0.6f, true),
                        new SkyCelestialBody(SkyCelestialBody.BodyType.MOON, 0.0f, 0.0f, 1.3f, true),
                        new SkyCelestialBody(SkyCelestialBody.BodyType.MOON, 40.0f, 70.0f, 0.7f, true)
                ));

        addSky("moon",
                GCDimensions.MOON_LEVEL_KEY.identifier(),
                false,
                List.of());
    }

    private void addSky(String path, Identifier dimension, boolean renderDarkDisc, List<SkyCelestialBody> bodies) {
        String qualifiedId = Constants.MOD_ID + ":" + path;
        skies.add(new DimensionSky(qualifiedId, dimension, renderDarkDisc, bodies));
    }

    @Override
    public @NonNull CompletableFuture<?> run(@NonNull CachedOutput cache) {
        List<CompletableFuture<?>> futures = new ArrayList<>();

        for (DimensionSky sky : skies) {
            String filePath = sky.id().contains(":")
                    ? sky.id().substring(sky.id().indexOf(':') + 1)
                    : sky.id();

            Identifier fileId = Identifier.fromNamespaceAndPath(Constants.MOD_ID, filePath);

            var encodeResult = DimensionSky.CODEC.encodeStart(JsonOps.INSTANCE, sky);
            encodeResult.ifSuccess(json -> {
                futures.add(DataProvider.saveStable(cache, json, pathProvider.json(fileId)));
            });
            encodeResult.ifError(err ->
                    Constants.LOG.error("Failed to encode dimension sky {}: {}", sky.id(), err.message())
            );
        }

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    @Override
    public @NonNull String getName() {
        return "Gallifreyan Chronicles Dimension Sky Provider";
    }
}
package com.noahtnt2009.gallifreyan_chronicles.datagen;

import com.mojang.serialization.JsonOps;
import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.init.GCDimensions;
import com.noahtnt2009.gallifreyan_chronicles.sky.DimensionSky;
import com.noahtnt2009.gallifreyan_chronicles.sky.ShaderDimension;
import com.noahtnt2009.gallifreyan_chronicles.sky.SkyBody;
import com.noahtnt2009.gallifreyan_chronicles.sky.SkyStars;
import com.noahtnt2009.gallifreyan_chronicles.util.GCUtils;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class GCDimensionSkyProvider implements DataProvider {
    private static final Identifier SUN = Identifier.withDefaultNamespace("textures/environment/celestial/sun.png");
    private static final Identifier FULL_MOON = Identifier.withDefaultNamespace("textures/environment/celestial/moon/full_moon.png");
    private static final Identifier EARTH = GCUtils.modIdentifier("textures/environment/earth.png");
    private static final int WHITE = 0xFFFFFFFF;

    private final PackOutput.PathProvider pathProvider;
    private final List<DimensionSky> skies = new ArrayList<>();

    public GCDimensionSkyProvider(PackOutput output) {
        this.pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "dimension_sky");
        registerDefaults();
    }

    private void registerDefaults() {
        addSky("gallifrey", GCDimensions.GALLIFREY_LEVEL_KEY.identifier(), 0xFFFF6600, Optional.empty(), Optional.empty(), List.of(
                new SkyBody(SUN, 200.0F, 55.0F, 0.0F, 18.0F, WHITE, SkyBody.Blend.ADDITIVE, true),
                new SkyBody(FULL_MOON, 20.0F, 30.0F, 0.0F, 26.0F, WHITE, SkyBody.Blend.ADDITIVE, true),
                new SkyBody(FULL_MOON, 60.0F, 45.0F, 0.0F, 14.0F, WHITE, SkyBody.Blend.ADDITIVE, true)
        ));

        addSky("moon", GCDimensions.MOON_LEVEL_KEY.identifier(), 0xFF000000, Optional.of(ShaderDimension.END),
                Optional.of(new SkyStars(4000, 10842L, 0.15F, 0.35F, 1.0F, WHITE, false)), List.of(
                        new SkyBody(SUN, 120.0F, 35.0F, 0.0F, 24.0F, WHITE, SkyBody.Blend.ADDITIVE, false),
                        new SkyBody(EARTH, 210.0F, 30.0F, 0.0F, 32.0F, WHITE, SkyBody.Blend.NORMAL, false)
                ));
    }

    private void addSky(String path, Identifier dimension, int skyColor, Optional<ShaderDimension> shaderDimension,
                        Optional<SkyStars> stars, List<SkyBody> bodies) {
        String qualifiedId = Constants.MOD_ID + ":" + path;
        skies.add(new DimensionSky(qualifiedId, dimension, Optional.of(skyColor), false, stars, bodies, shaderDimension));
    }

    @Override
    public @NonNull CompletableFuture<?> run(@NonNull CachedOutput cache) {
        List<CompletableFuture<?>> futures = new ArrayList<>();

        for (DimensionSky sky : skies) {
            String filePath = sky.id().contains(":")
                    ? sky.id().substring(sky.id().indexOf(':') + 1)
                    : sky.id();

            Identifier fileId = GCUtils.modIdentifier(filePath);

            var encodeResult = DimensionSky.CODEC.encodeStart(JsonOps.INSTANCE, sky);
            encodeResult.ifSuccess(json -> futures.add(DataProvider.saveStable(cache, json, pathProvider.json(fileId))));
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

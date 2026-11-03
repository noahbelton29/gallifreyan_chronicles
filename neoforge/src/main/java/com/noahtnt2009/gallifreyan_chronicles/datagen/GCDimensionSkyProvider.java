package com.noahtnt2009.gallifreyan_chronicles.datagen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.data.IdentifiableData;
import com.noahtnt2009.gallifreyan_chronicles.init.GCDimensions;
import com.noahtnt2009.gallifreyan_chronicles.sky.CelestialBody;
import com.noahtnt2009.gallifreyan_chronicles.sky.DimensionSky;
import com.noahtnt2009.gallifreyan_chronicles.sky.OverlayFog;
import com.noahtnt2009.gallifreyan_chronicles.sky.ShaderDimension;
import com.noahtnt2009.gallifreyan_chronicles.sky.ShaderSky;
import com.noahtnt2009.gallifreyan_chronicles.sky.SkyBody;
import com.noahtnt2009.gallifreyan_chronicles.sky.SkyOrbit;
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
    private static final Identifier SUN_TEXTURE = Identifier.withDefaultNamespace("textures/environment/celestial/sun.png");
    private static final Identifier FULL_MOON_TEXTURE = Identifier.withDefaultNamespace("textures/environment/celestial/moon/full_moon.png");
    private static final Identifier EARTH_TEXTURE = GCUtils.modIdentifier("textures/environment/earth.png");
    private static final Identifier SUN = GCUtils.modIdentifier("sun");
    private static final Identifier FULL_MOON = GCUtils.modIdentifier("full_moon");
    private static final Identifier EARTH = GCUtils.modIdentifier("earth");
    private static final int WHITE = 0xFFFFFFFF;

    private final PackOutput.PathProvider skyPathProvider;
    private final PackOutput.PathProvider bodyPathProvider;
    private final List<CelestialBody> bodies = new ArrayList<>();
    private final List<DimensionSky> skies = new ArrayList<>();

    public GCDimensionSkyProvider(PackOutput output) {
        this.skyPathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "dimension_sky");
        this.bodyPathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "celestial_body");
        registerBodies();
        registerSkies();
    }

    private void registerBodies() {
        addBody(SUN, SUN_TEXTURE, 20.0F, SkyBody.Blend.ADDITIVE, true);
        addBody(FULL_MOON, FULL_MOON_TEXTURE, 24.0F, SkyBody.Blend.ADDITIVE, true);
        addBody(EARTH, EARTH_TEXTURE, 32.0F, SkyBody.Blend.NORMAL, false);
    }

    private void registerSkies() {
        addSky("gallifrey", GCDimensions.GALLIFREY_LEVEL_KEY.identifier(), Optional.empty(), Optional.empty(),
                ShaderSky.PACK, Optional.empty(), Optional.empty(), List.of(
                        SkyBody.of(SUN).orbiting(new SkyOrbit(SkyOrbit.Follows.SUN, 0.0F, -25.0F)),
                        SkyBody.of(SUN).orbiting(new SkyOrbit(SkyOrbit.Follows.SUN, 12.0F, -25.0F))
                                .sized(13.0F).colored(0xFFFFD08A),
                        SkyBody.of(FULL_MOON).orbiting(new SkyOrbit(SkyOrbit.Follows.MOON, 0.0F, -14.0F))
                                .visibleDuring(SkyBody.Visibility.NIGHT),
                        SkyBody.of(FULL_MOON).orbiting(new SkyOrbit(SkyOrbit.Follows.MOON, 30.0F, 20.0F))
                                .sized(13.0F).visibleDuring(SkyBody.Visibility.NIGHT)
                ));

        addSky("moon", GCDimensions.MOON_LEVEL_KEY.identifier(), Optional.of(0xFF000000), Optional.of(ShaderDimension.OVERWORLD),
                ShaderSky.OVERLAY, Optional.of(new OverlayFog(0xFF000000, 0.1F, 0.7F)),
                Optional.of(new SkyStars(4000, 10842L, 0.15F, 0.35F, 1.0F, WHITE, false, 0.5F, 1.0F)), List.of(
                        SkyBody.of(SUN).at(120.0F, 35.0F).sized(24.0F).fadingInRain(false),
                        SkyBody.of(EARTH).at(210.0F, 30.0F)
                ));
    }

    private void addBody(Identifier id, Identifier texture, float size, SkyBody.Blend blend, boolean fadesInRain) {
        bodies.add(new CelestialBody(id.toString(), texture, size, WHITE, blend, fadesInRain));
    }

    private void addSky(String path, Identifier dimension, Optional<Integer> skyColor, Optional<ShaderDimension> shaderDimension,
                        ShaderSky shaderSky, Optional<OverlayFog> overlayFog, Optional<SkyStars> stars, List<SkyBody> skyBodies) {
        String qualifiedId = Constants.MOD_ID + ":" + path;
        skies.add(new DimensionSky(qualifiedId, dimension, skyColor, false, stars, skyBodies, shaderDimension, shaderSky, overlayFog));
    }

    @Override
    public @NonNull CompletableFuture<?> run(@NonNull CachedOutput cache) {
        List<CompletableFuture<?>> futures = new ArrayList<>();
        save(cache, futures, bodies, CelestialBody.CODEC, bodyPathProvider, "celestial body");
        save(cache, futures, skies, DimensionSky.CODEC, skyPathProvider, "dimension sky");
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    private static <T extends IdentifiableData<T>> void save(CachedOutput cache, List<CompletableFuture<?>> futures, List<T> values,
                                                            Codec<T> codec, PackOutput.PathProvider pathProvider, String label) {
        for (T value : values) {
            String filePath = value.id().contains(":") ? value.id().substring(value.id().indexOf(':') + 1) : value.id();
            Identifier fileId = GCUtils.modIdentifier(filePath);
            var encodeResult = codec.encodeStart(JsonOps.INSTANCE, value);
            encodeResult.ifSuccess(json -> futures.add(DataProvider.saveStable(cache, json, pathProvider.json(fileId))));
            encodeResult.ifError(err -> Constants.LOG.error("Failed to encode {} {}: {}", label, value.id(), err.message()));
        }
    }

    @Override
    public @NonNull String getName() {
        return "Gallifreyan Chronicles Dimension Sky Provider";
    }
}

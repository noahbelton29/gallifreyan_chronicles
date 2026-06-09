package com.noahtnt2009.gallifreyan_chronicles.world.region;

import com.mojang.datafixers.util.Pair;
import com.noahtnt2009.gallifreyan_chronicles.registry.GCBiomes;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import terrablender.api.ParameterUtils;
import terrablender.api.Region;
import terrablender.api.RegionType;
import terrablender.api.VanillaParameterOverlayBuilder;

import java.util.function.Consumer;

public class GallifreyRegion extends Region {
    public GallifreyRegion(Identifier name, int weight) {
        super(name, RegionType.OVERWORLD, weight);
    }

    @Override
    public void addBiomes(Registry<Biome> registry,
                          Consumer<Pair<Climate.ParameterPoint,
                                  ResourceKey<Biome>>> mapper) {

        VanillaParameterOverlayBuilder builder =
                new VanillaParameterOverlayBuilder();

        new ParameterUtils.ParameterPointListBuilder()
                .temperature(ParameterUtils.Temperature.HOT)
                .humidity(ParameterUtils.Humidity.ARID)
                .continentalness(ParameterUtils.Continentalness.INLAND)
                .erosion(
                        ParameterUtils.Erosion.EROSION_0,
                        ParameterUtils.Erosion.EROSION_2
                )
                .depth(
                        ParameterUtils.Depth.SURFACE,
                        ParameterUtils.Depth.FLOOR
                )
                .weirdness(
                        ParameterUtils.Weirdness.FULL_RANGE
                )
                .build()
                .forEach(point ->
                        builder.add(point, GCBiomes.GALLIFREYAN_DESERT));

        builder.build().forEach(mapper);
    }
}
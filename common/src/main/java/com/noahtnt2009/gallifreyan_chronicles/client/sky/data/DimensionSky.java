package com.noahtnt2009.gallifreyan_chronicles.client.sky.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.List;

public record DimensionSky(
        String id,
        Identifier dimension,
        boolean renderDarkDisc,
        List<SkyCelestialBody> bodies
) {
    public static final Codec<DimensionSky> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Identifier.CODEC.fieldOf("dimension").forGetter(DimensionSky::dimension),
            Codec.BOOL.optionalFieldOf("render_dark_disc", true).forGetter(DimensionSky::renderDarkDisc),
            SkyCelestialBody.CODEC.listOf().optionalFieldOf("bodies", List.of()).forGetter(DimensionSky::bodies)
    ).apply(inst, (dimension, renderDarkDisc, bodies) ->
            new DimensionSky("__unset__", dimension, renderDarkDisc, bodies)));

    public DimensionSky withId(String id) {
        return new DimensionSky(id, dimension, renderDarkDisc, bodies);
    }
}

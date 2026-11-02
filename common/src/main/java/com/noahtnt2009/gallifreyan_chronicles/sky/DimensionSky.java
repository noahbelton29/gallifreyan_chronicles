package com.noahtnt2009.gallifreyan_chronicles.sky;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.noahtnt2009.gallifreyan_chronicles.data.IdentifiableData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Optional;

public record DimensionSky(String id, Identifier dimension, Optional<Integer> skyColor, boolean darkDisc,
                           Optional<SkyStars> stars, List<SkyBody> bodies,
                           Optional<ShaderDimension> shaderDimension) implements IdentifiableData<DimensionSky> {
    private static final String UNSET_ID = "__unset__";

    public static final Codec<DimensionSky> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Identifier.CODEC.fieldOf("dimension").forGetter(DimensionSky::dimension),
            HexColor.CODEC.optionalFieldOf("sky_color").forGetter(DimensionSky::skyColor),
            Codec.BOOL.optionalFieldOf("dark_disc", false).forGetter(DimensionSky::darkDisc),
            SkyStars.CODEC.optionalFieldOf("stars").forGetter(DimensionSky::stars),
            SkyBody.CODEC.listOf().optionalFieldOf("bodies", List.of()).forGetter(DimensionSky::bodies),
            ShaderDimension.CODEC.optionalFieldOf("shader_dimension").forGetter(DimensionSky::shaderDimension)
    ).apply(inst, (dimension, skyColor, darkDisc, stars, bodies, shaderDimension) ->
            new DimensionSky(UNSET_ID, dimension, skyColor, darkDisc, stars, bodies, shaderDimension)));

    public static final StreamCodec<FriendlyByteBuf, DimensionSky> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, DimensionSky::id,
            ByteBufCodecs.fromCodec(CODEC), sky -> sky,
            (id, sky) -> sky.withId(id)
    );

    @Override
    public DimensionSky withId(String id) {
        return new DimensionSky(id, dimension, skyColor, darkDisc, stars, bodies, shaderDimension);
    }
}

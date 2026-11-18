package uk.nbforge.gallifreyan_chronicles.sky.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import uk.nbforge.gallifreyan_chronicles.codec.HexColorCodec;
import uk.nbforge.gallifreyan_chronicles.loader.IdentifiableData;
import uk.nbforge.gallifreyan_chronicles.sky.shader.ShaderDimension;
import uk.nbforge.gallifreyan_chronicles.sky.shader.ShaderSky;

public record DimensionSky(
        String id,
        Identifier dimension,
        Optional<Integer> skyColor,
        boolean darkDisc,
        Optional<SkyStars> stars,
        List<SkyBody> bodies,
        Optional<ShaderDimension> shaderDimension,
        ShaderSky shaderSky,
        Optional<OverlayFog> overlayFog)
        implements IdentifiableData<DimensionSky> {
    public static final String UNSET_ID = "__unset__";

    public static final Codec<DimensionSky> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                    Identifier.CODEC.fieldOf("dimension").forGetter(DimensionSky::dimension),
                    HexColorCodec.CODEC.optionalFieldOf("sky_color").forGetter(DimensionSky::skyColor),
                    Codec.BOOL.optionalFieldOf("dark_disc", false).forGetter(DimensionSky::darkDisc),
                    SkyCodec.STARS.optionalFieldOf("stars").forGetter(DimensionSky::stars),
                    SkyBody.CODEC.listOf().optionalFieldOf("bodies", List.of()).forGetter(DimensionSky::bodies),
                    SkyCodec.SHADER_DIMENSION
                            .optionalFieldOf("shader_dimension")
                            .forGetter(DimensionSky::shaderDimension),
                    SkyCodec.SHADER_SKY
                            .optionalFieldOf("shader_sky", ShaderSky.PACK)
                            .forGetter(DimensionSky::shaderSky),
                    SkyCodec.OVERLAY_FOG.optionalFieldOf("overlay_fog").forGetter(DimensionSky::overlayFog))
            .apply(
                    inst,
                    (dimension, skyColor, darkDisc, stars, bodies, shaderDimension, shaderSky, overlayFog) ->
                            new DimensionSky(
                                    DimensionSky.UNSET_ID,
                                    dimension,
                                    skyColor,
                                    darkDisc,
                                    stars,
                                    bodies,
                                    shaderDimension,
                                    shaderSky,
                                    overlayFog)));

    public static final StreamCodec<FriendlyByteBuf, DimensionSky> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            DimensionSky::id,
            ByteBufCodecs.fromCodec(CODEC),
            sky -> sky,
            (id, sky) -> sky.withId(id));

    @Override
    public DimensionSky withId(String id) {
        return new DimensionSky(
                id, dimension, skyColor, darkDisc, stars, bodies, shaderDimension, shaderSky, overlayFog);
    }
}

package uk.nbforge.gallifreyan_chronicles.codec.sky.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import uk.nbforge.gallifreyan_chronicles.codec.HexColorCodec;
import uk.nbforge.gallifreyan_chronicles.sky.data.DimensionSky;
import uk.nbforge.gallifreyan_chronicles.sky.data.OverlayFog;
import uk.nbforge.gallifreyan_chronicles.sky.data.SkyBody;
import uk.nbforge.gallifreyan_chronicles.sky.data.SkyStars;
import uk.nbforge.gallifreyan_chronicles.sky.shader.ShaderDimension;
import uk.nbforge.gallifreyan_chronicles.sky.shader.ShaderSky;
import uk.nbforge.gallifreyan_chronicles.codec.sky.SkyCodec;

public final class DimensionSkyCodec {
    public static final Codec<DimensionSky> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Identifier.CODEC.fieldOf("dimension").forGetter(DimensionSky::dimension),
            HexColorCodec.CODEC.optionalFieldOf("sky_color").forGetter(DimensionSky::skyColor),
            Codec.BOOL.optionalFieldOf("dark_disc", false).forGetter(DimensionSky::darkDisc),
            SkyCodec.STARS.optionalFieldOf("stars").forGetter(DimensionSky::stars),
            SkyBodyCodec.CODEC.listOf().optionalFieldOf("bodies", List.of()).forGetter(DimensionSky::bodies),
            SkyCodec.SHADER_DIMENSION.optionalFieldOf("shader_dimension").forGetter(DimensionSky::shaderDimension),
            SkyCodec.SHADER_SKY.optionalFieldOf("shader_sky", ShaderSky.PACK).forGetter(DimensionSky::shaderSky),
            SkyCodec.OVERLAY_FOG.optionalFieldOf("overlay_fog").forGetter(DimensionSky::overlayFog)
    ).apply(inst, (dimension, skyColor, darkDisc, stars, bodies, shaderDimension, shaderSky, overlayFog) ->
            new DimensionSky(DimensionSky.UNSET_ID, dimension, skyColor, darkDisc, stars, bodies, shaderDimension, shaderSky, overlayFog)));

    public static final StreamCodec<FriendlyByteBuf, DimensionSky> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, DimensionSky::id,
            ByteBufCodecs.fromCodec(CODEC), sky -> sky,
            (id, sky) -> sky.withId(id)
    );

    private DimensionSkyCodec() {
    }
}

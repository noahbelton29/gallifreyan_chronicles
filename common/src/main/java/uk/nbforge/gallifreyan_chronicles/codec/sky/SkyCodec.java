package uk.nbforge.gallifreyan_chronicles.codec.sky;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.StringRepresentable;
import uk.nbforge.gallifreyan_chronicles.codec.HexColorCodec;
import uk.nbforge.gallifreyan_chronicles.sky.data.OverlayFog;
import uk.nbforge.gallifreyan_chronicles.sky.data.SkyOrbit;
import uk.nbforge.gallifreyan_chronicles.sky.data.SkyStars;
import uk.nbforge.gallifreyan_chronicles.sky.shader.ShaderDimension;
import uk.nbforge.gallifreyan_chronicles.sky.shader.ShaderSky;

public final class SkyCodec {
    public static final Codec<OverlayFog> OVERLAY_FOG = RecordCodecBuilder.create(inst -> inst.group(
            HexColorCodec.CODEC.optionalFieldOf("color", 0xFF000000).forGetter(OverlayFog::color),
            Codec.floatRange(0.0F, 1.0F).optionalFieldOf("start", 0.3F).forGetter(OverlayFog::start),
            Codec.floatRange(0.05F, 1.5F).optionalFieldOf("end", 1.0F).forGetter(OverlayFog::end)
    ).apply(inst, OverlayFog::new));

    public static final Codec<SkyStars> STARS = RecordCodecBuilder.create(inst -> inst.group(
            Codec.intRange(0, 20000).optionalFieldOf("count", 1500).forGetter(SkyStars::count),
            Codec.LONG.optionalFieldOf("seed", 10842L).forGetter(SkyStars::seed),
            Codec.floatRange(0.01F, 5.0F).optionalFieldOf("min_size", 0.15F).forGetter(SkyStars::minSize),
            Codec.floatRange(0.01F, 5.0F).optionalFieldOf("max_size", 0.25F).forGetter(SkyStars::maxSize),
            Codec.floatRange(0.0F, 1.0F).optionalFieldOf("brightness", 1.0F).forGetter(SkyStars::brightness),
            HexColorCodec.CODEC.optionalFieldOf("color", 0xFFFFFFFF).forGetter(SkyStars::color),
            Codec.BOOL.optionalFieldOf("fades_in_rain", true).forGetter(SkyStars::fadesInRain),
            Codec.floatRange(0.0F, 1.0F).optionalFieldOf("twinkle", 0.0F).forGetter(SkyStars::twinkle),
            Codec.floatRange(0.0F, 10.0F).optionalFieldOf("twinkle_speed", 1.0F).forGetter(SkyStars::twinkleSpeed)
    ).apply(inst, SkyStars::new));

    public static final Codec<SkyOrbit.Follows> ORBIT_FOLLOWS = StringRepresentable.fromEnum(SkyOrbit.Follows::values);

    public static final Codec<SkyOrbit> ORBIT = RecordCodecBuilder.create(inst -> inst.group(
            ORBIT_FOLLOWS.optionalFieldOf("follows", SkyOrbit.Follows.SUN).forGetter(SkyOrbit::follows),
            Codec.floatRange(-360.0F, 360.0F).optionalFieldOf("offset", 0.0F).forGetter(SkyOrbit::offset),
            Codec.floatRange(-90.0F, 90.0F).optionalFieldOf("tilt", 0.0F).forGetter(SkyOrbit::tilt)
    ).apply(inst, SkyOrbit::new));

    public static final Codec<ShaderDimension> SHADER_DIMENSION = StringRepresentable.fromEnum(ShaderDimension::values);

    public static final Codec<ShaderSky> SHADER_SKY = StringRepresentable.fromEnum(ShaderSky::values);

    private SkyCodec() {
    }
}

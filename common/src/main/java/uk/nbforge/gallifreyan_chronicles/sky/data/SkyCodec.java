package uk.nbforge.gallifreyan_chronicles.sky.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.StringRepresentable;
import uk.nbforge.gallifreyan_chronicles.codec.HexColorCodec;
import uk.nbforge.gallifreyan_chronicles.sky.shader.ShaderDimension;
import uk.nbforge.gallifreyan_chronicles.sky.shader.ShaderSky;

public final class SkyCodec {
    public static final Codec<OverlayFog> OVERLAY_FOG = RecordCodecBuilder.create(inst -> inst.group(
                    HexColorCodec.CODEC.optionalFieldOf("color", 0xFF000000).forGetter(OverlayFog::color),
                    Codec.floatRange(0.0f, 1.0f).optionalFieldOf("start", 0.3f).forGetter(OverlayFog::start),
                    Codec.floatRange(0.05f, 1.5f).optionalFieldOf("end", 1.0f).forGetter(OverlayFog::end))
            .apply(inst, OverlayFog::new));

    public static final Codec<SkyStars> STARS = RecordCodecBuilder.create(inst -> inst.group(
                    Codec.intRange(0, 20000).optionalFieldOf("count", 1500).forGetter(SkyStars::count),
                    Codec.LONG.optionalFieldOf("seed", 10842L).forGetter(SkyStars::seed),
                    Codec.floatRange(0.01f, 5.0f)
                            .optionalFieldOf("min_size", 0.15f)
                            .forGetter(SkyStars::minSize),
                    Codec.floatRange(0.01f, 5.0f)
                            .optionalFieldOf("max_size", 0.25f)
                            .forGetter(SkyStars::maxSize),
                    Codec.floatRange(0.0f, 1.0f)
                            .optionalFieldOf("brightness", 1.0f)
                            .forGetter(SkyStars::brightness),
                    HexColorCodec.CODEC.optionalFieldOf("color", 0xFFFFFFFF).forGetter(SkyStars::color),
                    Codec.BOOL.optionalFieldOf("fades_in_rain", true).forGetter(SkyStars::fadesInRain),
                    Codec.floatRange(0.0f, 1.0f)
                            .optionalFieldOf("twinkle", 0.0f)
                            .forGetter(SkyStars::twinkle),
                    Codec.floatRange(0.0f, 10.0f)
                            .optionalFieldOf("twinkle_speed", 1.0f)
                            .forGetter(SkyStars::twinkleSpeed))
            .apply(inst, SkyStars::new));

    public static final Codec<SkyOrbit.Follows> ORBIT_FOLLOWS = StringRepresentable.fromEnum(SkyOrbit.Follows::values);

    public static final Codec<SkyOrbit> ORBIT = RecordCodecBuilder.create(inst -> inst.group(
                    ORBIT_FOLLOWS
                            .optionalFieldOf("follows", SkyOrbit.Follows.SUN)
                            .forGetter(SkyOrbit::follows),
                    Codec.floatRange(-360.0f, 360.0f)
                            .optionalFieldOf("offset", 0.0f)
                            .forGetter(SkyOrbit::offset),
                    Codec.floatRange(-90.0f, 90.0f)
                            .optionalFieldOf("tilt", 0.0f)
                            .forGetter(SkyOrbit::tilt))
            .apply(inst, SkyOrbit::new));

    public static final Codec<ShaderDimension> SHADER_DIMENSION = StringRepresentable.fromEnum(ShaderDimension::values);

    public static final Codec<ShaderSky> SHADER_SKY = StringRepresentable.fromEnum(ShaderSky::values);

    private SkyCodec() {}
}

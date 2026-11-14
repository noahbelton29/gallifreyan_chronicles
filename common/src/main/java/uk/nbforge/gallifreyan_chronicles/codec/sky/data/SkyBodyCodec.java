package uk.nbforge.gallifreyan_chronicles.codec.sky.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import uk.nbforge.gallifreyan_chronicles.codec.HexColorCodec;
import uk.nbforge.gallifreyan_chronicles.sky.data.SkyBody;
import uk.nbforge.gallifreyan_chronicles.sky.data.SkyOrbit;
import net.minecraft.util.StringRepresentable;
import uk.nbforge.gallifreyan_chronicles.codec.sky.SkyCodec;

public final class SkyBodyCodec {
    public static final Codec<SkyBody.Blend> BLEND = StringRepresentable.fromEnum(SkyBody.Blend::values);

    public static final Codec<SkyBody.Visibility> VISIBILITY = StringRepresentable.fromEnum(SkyBody.Visibility::values);

    public static final Codec<SkyBody> CODEC = RecordCodecBuilder.<SkyBody>create(inst -> inst.group(
            Identifier.CODEC.optionalFieldOf("body").forGetter(SkyBody::body),
            Identifier.CODEC.optionalFieldOf("texture").forGetter(SkyBody::texture),
            Codec.FLOAT.optionalFieldOf("yaw", 0.0F).forGetter(SkyBody::yaw),
            Codec.floatRange(-90.0F, 90.0F).optionalFieldOf("pitch", 0.0F).forGetter(SkyBody::pitch),
            Codec.FLOAT.optionalFieldOf("roll", 0.0F).forGetter(SkyBody::roll),
            Codec.floatRange(0.0F, 200.0F).optionalFieldOf("size").forGetter(SkyBody::size),
            HexColorCodec.CODEC.optionalFieldOf("color").forGetter(SkyBody::color),
            BLEND.optionalFieldOf("blend").forGetter(SkyBody::blend),
            Codec.BOOL.optionalFieldOf("fades_in_rain").forGetter(SkyBody::fadesInRain),
            SkyCodec.ORBIT.optionalFieldOf("orbit").forGetter(SkyBody::orbit),
            VISIBILITY.optionalFieldOf("visibility", SkyBody.Visibility.ALWAYS).forGetter(SkyBody::visibility)
    ).apply(inst, SkyBody::new)).validate(SkyBody::validate);

    private SkyBodyCodec() {
    }
}

package uk.nbforge.gallifreyan_chronicles.codec.client.sky;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import uk.nbforge.gallifreyan_chronicles.client.sky.StarDensity;

public final class StarDensityCodec {
    public static final Codec<StarDensity> CODEC = StringRepresentable.fromEnum(StarDensity::values);

    private StarDensityCodec() {
    }
}

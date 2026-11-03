package com.noahtnt2009.gallifreyan_chronicles.sky;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.StringRepresentable;

public record SkyOrbit(Follows follows, float offset, float tilt) {
    public static final Codec<SkyOrbit> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Follows.CODEC.optionalFieldOf("follows", Follows.SUN).forGetter(SkyOrbit::follows),
            Codec.floatRange(-360.0F, 360.0F).optionalFieldOf("offset", 0.0F).forGetter(SkyOrbit::offset),
            Codec.floatRange(-90.0F, 90.0F).optionalFieldOf("tilt", 0.0F).forGetter(SkyOrbit::tilt)
    ).apply(inst, SkyOrbit::new));

    public enum Follows implements StringRepresentable {
        SUN("sun"),
        MOON("moon");

        public static final Codec<Follows> CODEC = StringRepresentable.fromEnum(Follows::values);

        private final String name;

        Follows(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}

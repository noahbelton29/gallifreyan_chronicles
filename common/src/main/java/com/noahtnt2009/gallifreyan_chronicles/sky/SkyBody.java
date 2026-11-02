package com.noahtnt2009.gallifreyan_chronicles.sky;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;

public record SkyBody(Identifier texture, float yaw, float pitch, float roll, float size, int color,
                      Blend blend, boolean fadesInRain) {
    public static final float DEFAULT_SIZE = 20.0F;

    public static final Codec<SkyBody> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Identifier.CODEC.fieldOf("texture").forGetter(SkyBody::texture),
            Codec.FLOAT.optionalFieldOf("yaw", 0.0F).forGetter(SkyBody::yaw),
            Codec.floatRange(-90.0F, 90.0F).optionalFieldOf("pitch", 0.0F).forGetter(SkyBody::pitch),
            Codec.FLOAT.optionalFieldOf("roll", 0.0F).forGetter(SkyBody::roll),
            Codec.floatRange(0.0F, 200.0F).optionalFieldOf("size", DEFAULT_SIZE).forGetter(SkyBody::size),
            HexColor.CODEC.optionalFieldOf("color", 0xFFFFFFFF).forGetter(SkyBody::color),
            Blend.CODEC.optionalFieldOf("blend", Blend.NORMAL).forGetter(SkyBody::blend),
            Codec.BOOL.optionalFieldOf("fades_in_rain", true).forGetter(SkyBody::fadesInRain)
    ).apply(inst, SkyBody::new));

    public enum Blend implements StringRepresentable {
        NORMAL("normal"),
        ADDITIVE("additive");

        public static final Codec<Blend> CODEC = StringRepresentable.fromEnum(Blend::values);

        private final String name;

        Blend(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}

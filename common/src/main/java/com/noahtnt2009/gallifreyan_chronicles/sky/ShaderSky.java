package com.noahtnt2009.gallifreyan_chronicles.sky;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum ShaderSky implements StringRepresentable {
    PACK("pack"),
    OVERLAY("overlay");

    public static final Codec<ShaderSky> CODEC = StringRepresentable.fromEnum(ShaderSky::values);

    private final String name;

    ShaderSky(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}

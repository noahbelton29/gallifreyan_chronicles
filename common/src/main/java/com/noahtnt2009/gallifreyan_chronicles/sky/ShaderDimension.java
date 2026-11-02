package com.noahtnt2009.gallifreyan_chronicles.sky;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum ShaderDimension implements StringRepresentable {
    OVERWORLD("overworld"),
    NETHER("the_nether"),
    END("the_end");

    public static final Codec<ShaderDimension> CODEC = StringRepresentable.fromEnum(ShaderDimension::values);

    private final String name;

    ShaderDimension(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}

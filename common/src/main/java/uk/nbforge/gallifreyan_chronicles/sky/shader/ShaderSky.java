package uk.nbforge.gallifreyan_chronicles.sky.shader;

import net.minecraft.util.StringRepresentable;

public enum ShaderSky implements StringRepresentable {
    PACK("pack"),
    OVERLAY("overlay");

    private final String name;

    ShaderSky(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}

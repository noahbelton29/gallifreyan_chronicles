package uk.nbforge.gallifreyan_chronicles.sky.shader;

import net.minecraft.util.StringRepresentable;

public enum ShaderDimension implements StringRepresentable {
    OVERWORLD("overworld"),
    NETHER("the_nether"),
    END("the_end");

    private final String name;

    ShaderDimension(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}

package uk.nbforge.gallifreyan_chronicles.sky.data;

import net.minecraft.util.StringRepresentable;

public record SkyOrbit(Follows follows, float offset, float tilt) {
    public enum Follows implements StringRepresentable {
        SUN("sun"),
        MOON("moon");

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

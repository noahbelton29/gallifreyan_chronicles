package uk.nbforge.gallifreyan_chronicles.client.sky;

import net.minecraft.util.StringRepresentable;

import java.util.Locale;

public enum StarDensity implements StringRepresentable {
    OFF("off", 0.0F),
    LOW("low", 0.5F),
    NORMAL("normal", 1.0F),
    HIGH("high", 2.0F);

    private final String name;
    private final float multiplier;

    StarDensity(String name, float multiplier) {
        this.name = name;
        this.multiplier = multiplier;
    }

    public float multiplier() {
        return multiplier;
    }

    public String translationKey() {
        return "options.gallifreyan_chronicles.star_density." + name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public static StarDensity byName(String name) {
        if (name != null) {
            String lowered = name.toLowerCase(Locale.ROOT);
            for (StarDensity density : values()) {
                if (density.name.equals(lowered)) return density;
            }
        }
        return NORMAL;
    }
}

package uk.nbforge.gallifreyan_chronicles.client.sky;

import com.mojang.serialization.Codec;
import java.util.Locale;
import net.minecraft.util.StringRepresentable;

public enum StarDensity implements StringRepresentable {
    OFF("off", 0.0f),
    LOW("low", 0.5f),
    NORMAL("normal", 1.0f),
    HIGH("high", 2.0f);

    public static final Codec<StarDensity> CODEC = StringRepresentable.fromEnum(StarDensity::values);

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

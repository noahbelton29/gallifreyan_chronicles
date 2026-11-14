package uk.nbforge.gallifreyan_chronicles.sky.data;

import uk.nbforge.gallifreyan_chronicles.core.data.IdentifiableData;
import net.minecraft.resources.Identifier;

public record CelestialBody(String id, Identifier texture, float size, int color, SkyBody.Blend blend,
                            boolean fadesInRain) implements IdentifiableData<CelestialBody> {
    public static final String UNSET_ID = "__unset__";

    @Override
    public CelestialBody withId(String id) {
        return new CelestialBody(id, texture, size, color, blend, fadesInRain);
    }
}

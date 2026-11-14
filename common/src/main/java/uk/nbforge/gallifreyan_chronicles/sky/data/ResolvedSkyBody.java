package uk.nbforge.gallifreyan_chronicles.sky.data;

import net.minecraft.resources.Identifier;

import java.util.Optional;

public record ResolvedSkyBody(Identifier texture, float yaw, float pitch, float roll, float size, int color,
                              SkyBody.Blend blend, boolean fadesInRain, Optional<SkyOrbit> orbit,
                              SkyBody.Visibility visibility) {
}

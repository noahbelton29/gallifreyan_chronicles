package uk.nbforge.gallifreyan_chronicles.sky.data;

import java.util.Optional;
import net.minecraft.resources.Identifier;

public record ResolvedSkyBody(
        Identifier texture,
        float yaw,
        float pitch,
        float roll,
        float size,
        int color,
        SkyBody.Blend blend,
        boolean fadesInRain,
        Optional<SkyOrbit> orbit,
        SkyBody.Visibility visibility) {}

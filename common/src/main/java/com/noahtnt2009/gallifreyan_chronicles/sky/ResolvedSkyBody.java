package com.noahtnt2009.gallifreyan_chronicles.sky;

import net.minecraft.resources.Identifier;

import java.util.Optional;

public record ResolvedSkyBody(Identifier texture, float yaw, float pitch, float roll, float size, int color,
                              SkyBody.Blend blend, boolean fadesInRain, Optional<SkyOrbit> orbit,
                              SkyBody.Visibility visibility) {
}

package uk.nbforge.gallifreyan_chronicles.client.util;

import uk.nbforge.gallifreyan_chronicles.client.render.detail.RenderCapability;

/** Whether the client can show the finer rendering details, the custom door textures, the glow and the painted sky. */
public final class DetailLevelUtil {
    private DetailLevelUtil() {}

    public static boolean highDetail() {
        return RenderCapability.verified();
    }
}

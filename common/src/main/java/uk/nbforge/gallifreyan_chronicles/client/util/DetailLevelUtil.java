package uk.nbforge.gallifreyan_chronicles.client.util;

import uk.nbforge.gallifreyan_chronicles.client.renderer.detail.RenderCapability;

public final class DetailLevelUtil {
    private DetailLevelUtil() {}

    public static boolean lowDetail() {
        return RenderCapability.unverified();
    }
}

package uk.nbforge.gallifreyan_chronicles.compat.portal;

/**
 * Immersive Portals and Seamless Portals ship with debug probes that run on the render thread
 * around every portal crossing (per-frame string formatting, then a big log dump a few frames
 * after you cross). They only produce log output, so the portal compat mixins switch them off.
 * Start the game with -Dgc.portal_diagnostics=true to turn them back on while debugging portals.
 */
public final class PortalDiagnostics {
    private static final boolean ENABLED = Boolean.getBoolean("gc.portal_diagnostics");

    private PortalDiagnostics() {
    }

    public static boolean enabled() {
        return ENABLED;
    }
}

package uk.nbforge.gallifreyan_chronicles.compat.portal;

/**
 * immersive portals and seamless portals ship debug probes that run on the render thread around every portal crossing,
 * string formatting every frame then a massive log dump a few frames after u cross. theyre log only so the portal
 * compat mixins turn them off. start the game with gc.portal_diagnostics=true to get them back when debugging portals
 */
public final class PortalDiagnostics {
    private static final boolean ENABLED = Boolean.getBoolean("gc.portal_diagnostics");

    private PortalDiagnostics() {}

    public static boolean disabled() {
        return !ENABLED;
    }
}

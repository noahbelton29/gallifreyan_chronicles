package uk.nbforge.gallifreyan_chronicles.compat.portal;

/**
   Immersive Portals and Seamless Portals ship with debug probes that run on the render thread around every portal crossing, with string formatting every frame and then a big log dump a few frames after you cross.
   They only make log output so the portal compat mixins switch them off.
   Start the game with the property gc.portal_diagnostics set to true to turn them back on while debugging portals.
*/
public final class PortalDiagnostics {
    private static final boolean ENABLED = Boolean.getBoolean("gc.portal_diagnostics");

    private PortalDiagnostics() {
    }

    public static boolean enabled() {
        return ENABLED;
    }
}

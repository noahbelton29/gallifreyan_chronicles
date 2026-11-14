package uk.nbforge.gallifreyan_chronicles.client.gui.title;

public final class ReplayViewerHook {
    public static volatile Runnable opener;

    private ReplayViewerHook() {
    }

    public static boolean available() {
        return opener != null;
    }
}

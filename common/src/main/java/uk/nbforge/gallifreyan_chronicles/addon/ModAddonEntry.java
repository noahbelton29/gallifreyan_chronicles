package uk.nbforge.gallifreyan_chronicles.addon;

/** A mod that says it is an addon. */
public record ModAddonEntry(String modId, String className) {
    // The key addons use in their mod info
    public static final String KEY = "gallifreyan_chronicles:addon";
}

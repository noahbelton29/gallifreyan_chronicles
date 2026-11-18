package uk.nbforge.gallifreyan_chronicles.addon;

public record ModAddonEntry(String modId, String className) {
    public static final String KEY = "gallifreyan_chronicles:addon";
}

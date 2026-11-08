package com.noahtnt2009.gallifreyan_chronicles.addon;

/* a mod that says it's an addon */
public record ModAddonEntry(String modId, String className) {
    /* the key addons use in their mod info */
    public static final String KEY = "gallifreyan_chronicles:addon";
}

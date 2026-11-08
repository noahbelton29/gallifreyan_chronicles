package com.noahtnt2009.gallifreyan_chronicles.addon.api;

/** The version of the addon API itself. */
public final class AddonApi {
    /**
     * Bumped only when the API changes in a way that can break existing addons. An addon says which version it was
     * written for with {@code "api": 1} in its {@code addon.json}; one that asks for a newer API than this is not loaded.
     */
    public static final int VERSION = 1;

    private AddonApi() {
    }
}

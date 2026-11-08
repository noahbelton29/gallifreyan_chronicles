package com.noahtnt2009.gallifreyan_chronicles.addon.api;

public interface GCAddon {
    void onInitialize(AddonContext context);

    default void onShutdown() {
    }
}

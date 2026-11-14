package uk.nbforge.gallifreyan_chronicles.addon.api;

public interface GCAddon {
    void onInitialize(AddonContext context);

    default void onShutdown() {
    }
}

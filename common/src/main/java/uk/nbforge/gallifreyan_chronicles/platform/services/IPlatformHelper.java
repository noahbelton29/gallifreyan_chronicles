package uk.nbforge.gallifreyan_chronicles.platform.services;

import uk.nbforge.gallifreyan_chronicles.addon.ModAddonEntry;

import java.nio.file.Path;
import java.util.List;

public interface IPlatformHelper {

    /** The loader's config directory. */
    Path getConfigDir();

    /**
     * Gets the name of the current platform
     *
     * @return The name of the current platform.
     */
    String getPlatformName();

    /**
     * Checks if a mod with the given id is loaded.
     *
     * @param modId The mod to check if it is loaded.
     * @return True if the mod is loaded, false otherwise.
     */
    boolean isModLoaded(String modId);

    /**
     * Gets the version string of a loaded mod (use {@code "minecraft"} for the game itself).
     *
     * @param modId The mod to look up.
     * @return The version, or {@code "unknown"} if the mod is not loaded.
     */
    String getModVersion(String modId);

    /**
     * Check if the game is currently in a development environment.
     *
     * @return True if in a development environment, false otherwise.
     */
    boolean isDevelopmentEnvironment();

    /**
     * Gets the name of the environment type as a string.
     *
     * @return The name of the environment type.
     */
    default String getEnvironmentName() {

        return isDevelopmentEnvironment() ? "development" : "production";
    }

    /**
     * Finds every loaded mod that declares itself a Gallifreyan Chronicles addon, using the loader's own metadata:
     * a {@code "gallifreyan_chronicles:addon"} custom value in {@code fabric.mod.json}, or the same key as a mod
     * property in {@code neoforge.mods.toml}. The value is the name of the addon's {@code GCAddon} class.
     *
     * @return the addons, in no particular order
     */
    List<ModAddonEntry> findModAddons();
}

package uk.nbforge.gallifreyan_chronicles.platform.services;

import uk.nbforge.gallifreyan_chronicles.addon.ModAddonEntry;

import java.nio.file.Path;
import java.util.List;

public interface IPlatformHelper {

    Path getConfigDir();

    String getPlatformName();

    boolean isModLoaded(String modId);

    /** Use minecraft for the game itself, returns unknown if the mod is not loaded. */
    String getModVersion(String modId);

    boolean isDevelopmentEnvironment();

    default String getEnvironmentName() {

        return isDevelopmentEnvironment() ? "development" : "production";
    }

    /**
       Finds every loaded mod that says it is a Gallifreyan Chronicles addon, using the metadata of the loader.
       Fabric uses a custom value in fabric.mod.json and NeoForge uses the same key as a mod property in neoforge.mods.toml.
       The value is the name of the GCAddon class of the addon, the order is not defined.
    */
    List<ModAddonEntry> findModAddons();
}

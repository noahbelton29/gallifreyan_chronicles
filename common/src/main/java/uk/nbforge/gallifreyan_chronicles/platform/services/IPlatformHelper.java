package uk.nbforge.gallifreyan_chronicles.platform.services;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import uk.nbforge.gallifreyan_chronicles.addon.ModAddonEntry;

public interface IPlatformHelper {

    Path getConfigDir();

    String getPlatformName();

    boolean isModLoaded(String modId);

    /** use minecraft for the game itself, gives unknown if the mod isnt loaded */
    String getModVersion(String modId);

    boolean isDevelopmentEnvironment();

    default String getEnvironmentName() {

        return isDevelopmentEnvironment() ? "development" : "production";
    }

    /**
     * every loaded mod that says its a gallifreyan chronicles addon, from the loader metadata. fabric uses a custom
     * value in fabric.mod.json, neoforge uses the same key as a mod property in neoforge.mods.toml. value is the
     * addon's GCAddon class name, order isnt defined
     */
    List<ModAddonEntry> findModAddons();

    /**
     * reads files straight out of a mod's own jar, for stuff needed before datapacks exist (like registering blocks).
     * keyed by path from the jar root with / separators, only files under folder that keep accepts get read
     */
    Map<String, byte[]> readModFiles(String modId, String folder, Predicate<String> keep);
}

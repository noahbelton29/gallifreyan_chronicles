package uk.nbforge.gallifreyan_chronicles.platform;

import uk.nbforge.gallifreyan_chronicles.platform.services.IPlatformHelper;
import net.fabricmc.loader.api.FabricLoader;

import uk.nbforge.gallifreyan_chronicles.addon.ModAddonEntry;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FabricPlatformHelper implements IPlatformHelper {

    @Override
    public Path getConfigDir() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {

        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public String getModVersion(String modId) {
        return FabricLoader.getInstance().getModContainer(modId)
                .map(container -> container.getMetadata().getVersion().getFriendlyString())
                .orElse("unknown");
    }

    @Override
    public boolean isDevelopmentEnvironment() {

        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public List<ModAddonEntry> findModAddons() {
        List<ModAddonEntry> found = new ArrayList<>();
        for (ModContainer container : FabricLoader.getInstance().getAllMods()) {
            ModMetadata metadata = container.getMetadata();
            if (!metadata.containsCustomValue(ModAddonEntry.KEY)) continue;
            found.add(new ModAddonEntry(metadata.getId(), metadata.getCustomValue(ModAddonEntry.KEY).getAsString()));
        }
        return found;
    }
}

package uk.nbforge.gallifreyan_chronicles.platform;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforgespi.language.IModFileInfo;
import net.neoforged.neoforgespi.language.IModInfo;
import uk.nbforge.gallifreyan_chronicles.addon.ModAddonEntry;
import uk.nbforge.gallifreyan_chronicles.platform.services.IPlatformHelper;

public class NeoForgePlatformHelper implements IPlatformHelper {

    @Override
    public java.nio.file.Path getConfigDir() {
        return net.neoforged.fml.loading.FMLPaths.CONFIGDIR.get();
    }

    @Override
    public String getPlatformName() {

        return "NeoForge";
    }

    @Override
    public boolean isModLoaded(String modId) {

        return ModList.get().isLoaded(modId);
    }

    @Override
    public String getModVersion(String modId) {
        return ModList.get()
                .getModContainerById(modId)
                .map(container -> container.getModInfo().getVersion().toString())
                .orElse("unknown");
    }

    @Override
    public boolean isDevelopmentEnvironment() {

        return !FMLLoader.getCurrent().isProduction();
    }

    @Override
    public List<ModAddonEntry> findModAddons() {
        List<ModAddonEntry> found = new ArrayList<>();
        for (IModInfo info : ModList.get().getMods()) {
            Object className = info.getModProperties().get(ModAddonEntry.KEY);
            if (className != null) found.add(new ModAddonEntry(info.getModId(), className.toString()));
        }
        return found;
    }

    @Override
    public Map<String, byte[]> readModFiles(String modId, String folder, Predicate<String> keep) {
        Map<String, byte[]> found = new LinkedHashMap<>();
        IModFileInfo file = ModList.get().getModFileById(modId);
        if (file == null) return found;
        file.getFile().getContents().visitContent(folder, (path, resource) -> {
            if (!keep.test(path)) return;
            try {
                found.putIfAbsent(path, resource.readAllBytes());
            } catch (IOException e) {
                throw new UncheckedIOException("couldn't read " + path + " of " + modId, e);
            }
        });
        return found;
    }
}

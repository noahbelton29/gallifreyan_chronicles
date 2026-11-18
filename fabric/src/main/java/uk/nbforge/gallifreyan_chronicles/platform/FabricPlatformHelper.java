package uk.nbforge.gallifreyan_chronicles.platform;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import uk.nbforge.gallifreyan_chronicles.addon.ModAddonEntry;
import uk.nbforge.gallifreyan_chronicles.platform.services.IPlatformHelper;

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
        return FabricLoader.getInstance()
                .getModContainer(modId)
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
            found.add(new ModAddonEntry(
                    metadata.getId(), metadata.getCustomValue(ModAddonEntry.KEY).getAsString()));
        }
        return found;
    }

    @Override
    public Map<String, byte[]> readModFiles(String modId, String folder, Predicate<String> keep) {
        Map<String, byte[]> found = new LinkedHashMap<>();
        ModContainer container =
                FabricLoader.getInstance().getModContainer(modId).orElse(null);
        if (container == null) return found;
        for (Path root : container.getRootPaths()) {
            Path start = root.resolve(folder);
            if (!Files.isDirectory(start)) continue;
            try (Stream<Path> files = Files.walk(start)) {
                for (Path file : files.filter(Files::isRegularFile).toList()) {
                    String path = root.relativize(file).toString().replace('\\', '/');
                    if (keep.test(path)) found.putIfAbsent(path, Files.readAllBytes(file));
                }
            } catch (IOException e) {
                throw new UncheckedIOException("couldn't read " + folder + " of " + modId, e);
            }
        }
        return found;
    }
}

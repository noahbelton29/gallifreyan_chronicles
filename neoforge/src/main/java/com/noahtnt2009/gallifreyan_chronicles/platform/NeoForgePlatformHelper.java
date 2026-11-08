package com.noahtnt2009.gallifreyan_chronicles.platform;

import com.noahtnt2009.gallifreyan_chronicles.platform.services.IPlatformHelper;
import com.noahtnt2009.gallifreyan_chronicles.addon.ModAddonEntry;
import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.IModInfo;

import java.util.ArrayList;
import java.util.List;
import net.neoforged.fml.loading.FMLLoader;

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
        return ModList.get().getModContainerById(modId)
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
}

package com.noahtnt2009.gallifreyan_chronicles.addon;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;

import java.nio.file.Files;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Gives every enabled addon to a pack repository as a required pack, so an addon that is switched on is always loaded
 * and one that is switched off is simply not offered. Added to the resource pack repository and to every datapack
 * repository by {@code PackRepositoryMixin}.
 */
public final class AddonPackSource implements RepositorySource {
    public static final String ID_PREFIX = "gc_addon/";

    private final PackType type;

    public AddonPackSource(PackType type) {
        this.type = type;
    }

    @Override
    public void loadPacks(Consumer<Pack> output) {
        for (AddonInfo info : AddonManager.enabledData()) {
            try {
                Pack.ResourcesSupplier resources = Files.isDirectory(info.path())
                        ? new PathPackResources.PathResourcesSupplier(info.path())
                        : new FilePackResources.FileResourcesSupplier(info.path());
                PackLocationInfo location = new PackLocationInfo(ID_PREFIX + info.id(), Component.literal(info.name()),
                        PackSource.BUILT_IN, Optional.empty());
                Pack pack = Pack.readMetaAndCreate(location, resources, type,
                        new PackSelectionConfig(true, Pack.Position.TOP, false));
                if (pack != null) output.accept(pack);
            } catch (RuntimeException e) {
                Constants.LOG.warn("Could not load the data of addon '{}'", info.id(), e);
            }
        }
    }
}

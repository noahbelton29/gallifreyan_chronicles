package uk.nbforge.gallifreyan_chronicles.loader;

import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.loader.GCJsonReloadListener;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;

public final class GCFabricLoaders {
    private GCFabricLoaders() {}

    public static void register(Identifier id, GCJsonReloadListener<?> loader) {
        ResourceLoader.get(PackType.SERVER_DATA).registerReloadListener(id, loader);
        Constants.LOG.info("Registered GC Fabric Reload Listener: {}", id);
    }
}

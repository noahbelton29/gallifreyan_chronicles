package uk.nbforge.gallifreyan_chronicles.loader;

import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import uk.nbforge.gallifreyan_chronicles.util.GCLog;

public final class GCFabricLoaders {
    private GCFabricLoaders() {}

    public static void register(Identifier id, GCJsonReloadListener<?> loader) {
        ResourceLoader.get(PackType.SERVER_DATA).registerReloadListener(id, loader);
        GCLog.DATA.info("Registered GC Fabric Reload Listener: {}", id);
    }
}

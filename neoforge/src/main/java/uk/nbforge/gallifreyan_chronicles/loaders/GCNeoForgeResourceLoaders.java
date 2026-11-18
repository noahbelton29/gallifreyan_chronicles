package uk.nbforge.gallifreyan_chronicles.loaders;

import uk.nbforge.gallifreyan_chronicles.loader.GCDataLoaders;
import uk.nbforge.gallifreyan_chronicles.util.GCLog;

public class GCNeoForgeResourceLoaders {
    public static void registerResourceLoaders() {
        for (GCDataLoaders.Entry entry : GCDataLoaders.ALL) {
            GCNeoForgeLoaders.register(entry.path(), entry.factory());
        }

        GCLog.DATA.info("Registered GC Resource Loaders (NeoForge)");
    }
}

package uk.nbforge.gallifreyan_chronicles.loaders;

import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.loader.GCDataLoaders;

public class GCNeoForgeResourceLoaders {
    public static void registerResourceLoaders() {
        for (GCDataLoaders.Entry entry : GCDataLoaders.ALL) {
            GCNeoForgeLoaders.register(entry.path(), entry.factory());
        }

        Constants.LOG.info("Registered GC Resource Loaders (NeoForge)");
    }
}

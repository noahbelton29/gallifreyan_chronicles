package uk.nbforge.gallifreyan_chronicles.loader;

import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.util.GCUtil;

public class GCResourceLoaders {
    public static void registerResourceLoaders() {
        for (GCDataLoaders.Entry entry : GCDataLoaders.ALL) {
            GCFabricLoaders.register(GCUtil.modIdentifier(entry.path()), entry.factory().get());
        }

        Constants.LOG.info("Registered GC Resource Loaders (Fabric)");
    }
}

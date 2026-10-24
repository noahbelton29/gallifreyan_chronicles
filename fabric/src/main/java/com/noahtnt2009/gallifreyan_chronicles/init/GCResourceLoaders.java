package com.noahtnt2009.gallifreyan_chronicles.init;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.data.GCDataLoaders;
import com.noahtnt2009.gallifreyan_chronicles.loader.GCFabricLoaders;
import com.noahtnt2009.gallifreyan_chronicles.util.GCUtils;

public class GCResourceLoaders {
    public static void registerResourceLoaders() {
        for (GCDataLoaders.Entry entry : GCDataLoaders.ALL) {
            GCFabricLoaders.register(GCUtils.modIdentifier(entry.path()), entry.factory().get());
        }

        Constants.LOG.info("Registered GC Resource Loaders (Fabric)");
    }
}

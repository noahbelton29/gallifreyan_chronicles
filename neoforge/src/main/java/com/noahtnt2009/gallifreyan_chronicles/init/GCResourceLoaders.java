package com.noahtnt2009.gallifreyan_chronicles.init;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.data.GCDataLoaders;
import com.noahtnt2009.gallifreyan_chronicles.event_subscriber.GCNeoForgeLoaders;

public class GCResourceLoaders {
    public static void registerResourceLoaders() {
        for (GCDataLoaders.Entry entry : GCDataLoaders.ALL) {
            GCNeoForgeLoaders.register(entry.path(), entry.factory());
        }

        Constants.LOG.info("Registered GC Resource Loaders (NeoForge)");
    }
}

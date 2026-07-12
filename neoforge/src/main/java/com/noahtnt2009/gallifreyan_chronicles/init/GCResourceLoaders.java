package com.noahtnt2009.gallifreyan_chronicles.init;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.client.sky.data.DimensionSkyLoader;
import com.noahtnt2009.gallifreyan_chronicles.event_subscriber.GCNeoForgeLoaders;
import com.noahtnt2009.gallifreyan_chronicles.tardis.console.TardisConsoleLoader;
import com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.TardisExteriorLoader;

public class GCResourceLoaders {
    public static void registerResourceLoaders() {
        GCNeoForgeLoaders.register("tardis_exteriors", TardisExteriorLoader::new);
        GCNeoForgeLoaders.register("tardis_consoles", TardisConsoleLoader::new);
        GCNeoForgeLoaders.register("dimension_sky", DimensionSkyLoader::new);

        Constants.LOG.info("Registered GC Resource Loaders (NeoForge)");
    }
}

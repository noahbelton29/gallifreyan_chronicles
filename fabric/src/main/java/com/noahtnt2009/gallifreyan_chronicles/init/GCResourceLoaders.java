package com.noahtnt2009.gallifreyan_chronicles.init;

import com.noahtnt2009.gallifreyan_chronicles.client.sky.data.DimensionSkyLoader;
import com.noahtnt2009.gallifreyan_chronicles.loader.GCFabricLoaders;
import com.noahtnt2009.gallifreyan_chronicles.tardis.console.TardisConsoleLoader;
import com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.TardisExteriorLoader;
import com.noahtnt2009.gallifreyan_chronicles.util.GCUtils;

public class GCResourceLoaders {
    public static void registerResourceLoaders() {
        GCFabricLoaders.register(
                GCUtils.of("tardis_exteriors"),
                new TardisExteriorLoader());

        GCFabricLoaders.register(
                GCUtils.of("tardis_consoles"),
                new TardisConsoleLoader());

        GCFabricLoaders.register(
                GCUtils.of("dimension_sky"),
                new DimensionSkyLoader());
    }
}

package com.noahtnt2009.gallifreyan_chronicles.data;

import com.noahtnt2009.gallifreyan_chronicles.client.sky.data.DimensionSkyLoader;
import com.noahtnt2009.gallifreyan_chronicles.tardis.console.TardisConsoleLoader;
import com.noahtnt2009.gallifreyan_chronicles.tardis.console.TardisConsoleVariantLoader;
import com.noahtnt2009.gallifreyan_chronicles.tardis.door.TardisDoorVariantLoader;
import com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.TardisExteriorLoader;
import com.noahtnt2009.gallifreyan_chronicles.tardis.exterior.fade.TardisFadeLoader;
import com.noahtnt2009.gallifreyan_chronicles.tardis.interior.TardisInteriorLoader;

import java.util.List;
import java.util.function.Supplier;

public final class GCDataLoaders {
    public record Entry(String path, Supplier<GCJsonReloadListener<?>> factory) {
    }

    public static final List<Entry> ALL = List.of(
            new Entry("tardis_exteriors", TardisExteriorLoader::new),
            new Entry("tardis_fades", TardisFadeLoader::new),
            new Entry("tardis_interiors", TardisInteriorLoader::new),
            new Entry("tardis_consoles", TardisConsoleLoader::new),
            new Entry("tardis_console_variants", TardisConsoleVariantLoader::new),
            new Entry("tardis_door_variants", TardisDoorVariantLoader::new),
            new Entry("dimension_sky", DimensionSkyLoader::new)
    );

    private GCDataLoaders() {
    }
}

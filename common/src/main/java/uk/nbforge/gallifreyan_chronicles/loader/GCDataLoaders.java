package uk.nbforge.gallifreyan_chronicles.loader;

import uk.nbforge.gallifreyan_chronicles.loader.sky.CelestialBodyLoader;
import uk.nbforge.gallifreyan_chronicles.loader.sky.DimensionSkyLoader;
import uk.nbforge.gallifreyan_chronicles.tardis.loader.TardisConsoleLoader;
import uk.nbforge.gallifreyan_chronicles.tardis.loader.TardisConsoleVariantLoader;
import uk.nbforge.gallifreyan_chronicles.tardis.loader.TardisDoorVariantLoader;
import uk.nbforge.gallifreyan_chronicles.tardis.loader.TardisExteriorLoader;
import uk.nbforge.gallifreyan_chronicles.tardis.loader.TardisPersonalityLoader;
import uk.nbforge.gallifreyan_chronicles.tardis.loader.TardisTraitLoader;
import uk.nbforge.gallifreyan_chronicles.tardis.loader.TardisFadeLoader;
import uk.nbforge.gallifreyan_chronicles.tardis.loader.TardisInteriorLoader;

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
            new Entry("tardis_personalities", TardisPersonalityLoader::new),
            new Entry("tardis_traits", TardisTraitLoader::new),
            new Entry("celestial_body", CelestialBodyLoader::new),
            new Entry("dimension_sky", DimensionSkyLoader::new)
    );

    private GCDataLoaders() {
    }
}

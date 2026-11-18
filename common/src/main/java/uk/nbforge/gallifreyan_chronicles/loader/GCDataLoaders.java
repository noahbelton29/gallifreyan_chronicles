package uk.nbforge.gallifreyan_chronicles.loader;

import com.mojang.serialization.JsonOps;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.server.MinecraftServer;
import uk.nbforge.gallifreyan_chronicles.Constants;
import uk.nbforge.gallifreyan_chronicles.core.data_registry.CelestialBodyRegistry;
import uk.nbforge.gallifreyan_chronicles.core.data_registry.DimensionSkyRegistry;
import uk.nbforge.gallifreyan_chronicles.core.data_registry.TardisConsoleRegistry;
import uk.nbforge.gallifreyan_chronicles.core.data_registry.TardisDoorRegistry;
import uk.nbforge.gallifreyan_chronicles.core.data_registry.TardisExteriorRegistry;
import uk.nbforge.gallifreyan_chronicles.core.data_registry.TardisInteriorRegistry;
import uk.nbforge.gallifreyan_chronicles.network.DimensionSkySyncPayload;
import uk.nbforge.gallifreyan_chronicles.network.tardis.TardisConsoleSyncPayload;
import uk.nbforge.gallifreyan_chronicles.network.tardis.TardisExteriorSyncPayload;
import uk.nbforge.gallifreyan_chronicles.network.tardis.TardisInteriorSyncPayload;
import uk.nbforge.gallifreyan_chronicles.platform.Services;
import uk.nbforge.gallifreyan_chronicles.sky.data.CelestialBody;
import uk.nbforge.gallifreyan_chronicles.sky.data.DimensionSky;
import uk.nbforge.gallifreyan_chronicles.tardis.Tardis;
import uk.nbforge.gallifreyan_chronicles.tardis.console.ConsoleLink;
import uk.nbforge.gallifreyan_chronicles.tardis.console.TardisConsole;
import uk.nbforge.gallifreyan_chronicles.tardis.console.TardisConsoleValidator;
import uk.nbforge.gallifreyan_chronicles.tardis.console.TardisConsoleVariant;
import uk.nbforge.gallifreyan_chronicles.tardis.door.TardisDoorVariant;
import uk.nbforge.gallifreyan_chronicles.tardis.exterior.TardisExterior;
import uk.nbforge.gallifreyan_chronicles.tardis.exterior.fade.FadeType;
import uk.nbforge.gallifreyan_chronicles.tardis.exterior.fade.FadeTypeKindCodec;
import uk.nbforge.gallifreyan_chronicles.tardis.exterior.fade.FadeTypes;
import uk.nbforge.gallifreyan_chronicles.tardis.interior.TardisInterior;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.TardisPersonality;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.TardisTrait;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.data.TardisPersonalities;
import uk.nbforge.gallifreyan_chronicles.tardis.mind.data.TardisTraits;

public final class GCDataLoaders {
    public record Entry(String path, Supplier<GCJsonReloadListener<?>> factory) {}

    public static final List<Entry> ALL = List.of(
            new Entry(
                    "tardis_exteriors",
                    () -> new GCJsonReloadListener<>(
                                    TardisExterior.CODEC,
                                    "tardis_exterior",
                                    "TARDIS Exterior",
                                    TardisExteriorRegistry::clear,
                                    TardisExteriorRegistry::register,
                                    TardisExteriorRegistry::size)
                            .afterReload(GCDataLoaders::afterExteriors)
                            .syncWith(GCDataLoaders::syncExteriors)),
            new Entry(
                    "tardis_fades",
                    () -> new GCJsonReloadListener<>(
                                    FadeTypeKindCodec.CODEC,
                                    "tardis_fade",
                                    "TARDIS Fade",
                                    FadeTypes::clear,
                                    GCDataLoaders::registerFade,
                                    FadeTypes::loadedSize)
                            .afterReload(FadeTypes::markReloaded)),
            new Entry(
                    "tardis_interiors",
                    () -> new GCJsonReloadListener<>(
                                    TardisInterior.CODEC,
                                    "tardis_interior",
                                    "TARDIS Interior",
                                    TardisInteriorRegistry::clear,
                                    TardisInteriorRegistry::register,
                                    TardisInteriorRegistry::size)
                            .afterReload(GCDataLoaders::afterInteriors)
                            .syncWith(GCDataLoaders::syncInteriors)),
            new Entry(
                    "tardis_consoles",
                    () -> new GCJsonReloadListener<>(
                                    TardisConsole.CODEC,
                                    "tardis_console",
                                    "TARDIS Console",
                                    TardisConsoleRegistry::clear,
                                    TardisConsoleRegistry::register,
                                    TardisConsoleRegistry::size)
                            .afterReload(GCDataLoaders::afterConsoles)
                            .syncWith(GCDataLoaders::syncConsoles)),
            new Entry(
                    "tardis_console_variants",
                    () -> new GCJsonReloadListener<>(
                                    TardisConsoleVariant.CODEC,
                                    "tardis_console_variant",
                                    "TARDIS Console Variant",
                                    TardisConsoleRegistry::clearVariants,
                                    TardisConsoleRegistry::registerVariant,
                                    TardisConsoleRegistry::variantSize)
                            .afterReload(TardisConsoleRegistry::validateVariants)
                            .syncWith(GCDataLoaders::syncConsoles)),
            new Entry(
                    "tardis_door_variants",
                    () -> new GCJsonReloadListener<>(
                                    TardisDoorVariant.CODEC,
                                    TardisDoorVariant.FOLDER,
                                    "TARDIS Door Variant",
                                    TardisDoorRegistry::clear,
                                    TardisDoorRegistry::register,
                                    TardisDoorRegistry::size)
                            .afterReload(TardisDoorRegistry::validate)
                            .syncWith(GCDataLoaders::syncConsoles)),
            new Entry(
                    "tardis_personalities",
                    () -> new GCJsonReloadListener<>(
                            TardisPersonality.CODEC,
                            "tardis_personality",
                            "TARDIS Personality",
                            TardisPersonalities::clear,
                            TardisPersonalities::register,
                            TardisPersonalities::size)),
            new Entry(
                    "tardis_traits",
                    () -> new GCJsonReloadListener<>(
                            TardisTrait.CODEC,
                            "tardis_trait",
                            "TARDIS Trait",
                            TardisTraits::clear,
                            TardisTraits::register,
                            TardisTraits::size)),
            new Entry(
                    "celestial_body",
                    () -> new GCJsonReloadListener<>(
                                    CelestialBody.CODEC,
                                    "celestial_body",
                                    "Celestial Body",
                                    CelestialBodyRegistry::clear,
                                    CelestialBodyRegistry::register,
                                    CelestialBodyRegistry::size)
                            .syncWith(GCDataLoaders::syncSkies)),
            new Entry(
                    "dimension_sky",
                    () -> new GCJsonReloadListener<>(
                                    DimensionSky.CODEC,
                                    "dimension_sky",
                                    "Dimension Sky",
                                    DimensionSkyRegistry::clear,
                                    DimensionSkyRegistry::register,
                                    DimensionSkyRegistry::size)
                            .syncWith(GCDataLoaders::syncSkies)));

    private GCDataLoaders() {}

    private static void afterExteriors() {
        TardisExterior def = TardisExteriorRegistry.get(TardisExteriorRegistry.defaultId());
        if (def == null) {
            Constants.LOG.warn(
                    "No TARDIS exteriors loaded! Expected e.g. data/{}/tardis_exterior/first_doctors_exterior.json",
                    Constants.MOD_ID);
            return;
        }
        TardisExteriorRegistry.setDefault(def);
    }

    private static void afterInteriors() {
        if (!TardisInteriorRegistry.contains(TardisInteriorRegistry.defaultId())) {
            Constants.LOG.warn(
                    "Default TARDIS interior {} not found; expected data/{}/tardis_interior/first_doctors_interior.json",
                    TardisInteriorRegistry.defaultId(),
                    Constants.MOD_ID);
        }
    }

    private static void afterConsoles() {
        if (TardisConsoleRegistry.size() == 0) {
            Constants.LOG.error(
                    "No TARDIS consoles loaded; console features are disabled until the data errors are fixed");
            TardisConsoleRegistry.setDefault(null);
            return;
        }
        TardisConsoleRegistry.setDefault(TardisConsoleRegistry.get(TardisConsoleRegistry.defaultId()));
        TardisConsoleRegistry.validateVariants();
        TardisConsoleValidator.validate(TardisConsoleRegistry.getAll());

        MinecraftServer server = GCJsonReloadListener.getCurrentServer();
        if (server == null) return;
        for (Tardis tardis : Tardis.all(server)) {
            tardis.consoles().forEach(ConsoleLink::spawnDefaultControls);
        }
    }

    private static void registerFade(FadeType fade) {
        FadeTypes.register(fade);
        Constants.LOG.info(
                "Registered TARDIS fade {} ({})",
                fade.id(),
                FadeTypeKindCodec.CODEC
                        .encodeStart(JsonOps.INSTANCE, fade)
                        .result()
                        .map(Object::toString)
                        .orElse("?"));
    }

    private static void syncExteriors(MinecraftServer server) {
        Services.NETWORK.broadcast(server, TardisExteriorSyncPayload.create());
    }

    private static void syncInteriors(MinecraftServer server) {
        Services.NETWORK.broadcast(server, TardisInteriorSyncPayload.create());
    }

    private static void syncConsoles(MinecraftServer server) {
        Services.NETWORK.broadcast(server, TardisConsoleSyncPayload.create());
    }

    private static void syncSkies(MinecraftServer server) {
        Services.NETWORK.broadcast(server, DimensionSkySyncPayload.create());
    }
}
